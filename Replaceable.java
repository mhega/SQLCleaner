import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.reflect.*;

public class Replaceable implements Serializable
{
	private String regex;
	private String replacement;
	private String callMethodToReplace;
	private static Hashtable<Double,String> commentStates;
	private static String memoryAddressRegexp = "([\"]?+([.][.][.])?+[ ]*?\n)?+0[xX][0-9a-fA-F]+?:[ ]*[\"]?+";



	private static Replaceable[] listOfDefaultReplaceables = new Replaceable[9];
	//Make sure to set the array length accordingly after adding new replaceables

	public Replaceable(String regex, String replacement, String callMethodToReplace)
	{
		this.regex = regex;
		this.replacement = replacement;
		this.callMethodToReplace = callMethodToReplace;
	}
	
	private boolean replaceViaCallMethod()
	{
		return !("".equals(callMethodToReplace));
	}

	public static String replace(String input) throws Exception
	{


		int listCounter = 0;

//####################################################################################################################
//####### (Note: The tool is very sensitive to order of replacements. i.e, order of Replaceables in the array) #######
//####################################################################################################################

		boolean hasMemoryAddresses = hasMemoryAddresses(input);
		if(hasMemoryAddresses)
		{
			listOfDefaultReplaceables[listCounter++] = new Replaceable("","","clearLastDoubleQuote");
			listOfDefaultReplaceables[listCounter++] = new Replaceable(memoryAddressRegexp,"","");
			
		}

		listOfDefaultReplaceables[listCounter++] = new Replaceable("","","replaceAllWithNTimes");
		listOfDefaultReplaceables[listCounter++] = new Replaceable("","","markCommentedLines");

		if(!hasMemoryAddresses)
		{
			listOfDefaultReplaceables[listCounter++] = new Replaceable("","","clearNewLines");
		}

		listOfDefaultReplaceables[listCounter++] = new Replaceable(("\\\\n"),"\n","");
		listOfDefaultReplaceables[listCounter++] = new Replaceable(("\\\\r"),"\n","");
		listOfDefaultReplaceables[listCounter++] = new Replaceable(("\\\\t"),"\t","");
		listOfDefaultReplaceables[listCounter++] = new Replaceable(("\\\\\""),"\"","");
		listOfDefaultReplaceables[listCounter++] = new Replaceable("","","restoreCommentedLinesStatus");

//####################################################################################################################


		for (int i=0 ; i<listCounter ; i++) //Using listCounter variable in the for loop rather than listOfDefaultReplaceables.length to avoid IndexArrayOutofboundsException
		{
			if(listOfDefaultReplaceables[i].replaceViaCallMethod())
			{
				input = callMethod(listOfDefaultReplaceables[i].callMethodToReplace,input);
			}
			else
			{
				input = input.replaceAll(listOfDefaultReplaceables[i].regex, listOfDefaultReplaceables[i].replacement);	
			}
		}

		return input;
	}

	private static String callMethod(String methodName, String input) throws Exception
	{
		String output = "";
		output = (String)(Class.forName("Replaceable").getMethod(methodName, Class.forName("java.lang.String")).invoke(null, (Object)input));
		return output;
	}


//####################################################################################################################
//####### (Note: All the methods that are being called from callMethod have to be public even though they are in #####
//####### the same class. Using getMethod method failed to identify private classes                              #####
//####################################################################################################################


	public static String markCommentedLines(String input)
	{
		commentStates = new Hashtable<java.lang.Double, String>();
		double mark;
		String occurance;
		String output="";
		Matcher p = Pattern.compile("--.*?(\\\\r|\\\\n|\n)").matcher(input); //.*? is used for the expression to select the smallest match. Regexps by default pick the largest match
		
		
		while(p.find())
		{
			mark = Math.random();
			//System.out.println(p.start()+" "+p.end());
			occurance = input.substring(p.start(),p.end());
			//System.out.println(occurance);
			commentStates.put(new Double(mark),occurance);
		}
		Enumeration keys = commentStates.keys();
		while(keys.hasMoreElements()) //Replace all occurances of comments in the string with the corresponding random marks.
		//Had to do that in a separate loop, as altering the input in the same loop above messed up with the whole thing. So had to make sure the string is unchanged during teh loop execution 
		{
			Double d = (Double)(keys.nextElement());
			String s = (String)(commentStates.get(d));
			input=input.replace((String)s, Double.toString(d)); //Using replace() not replaceAll() since we are dealing with literal sting here. replace() is new in JDK1.5
			//Here dealing with the string (comment) as a literal string (not regular expression) is important since we don't know what's in there
		}
		return input;	
	}

	public static String restoreCommentedLinesStatus(String input)
	{
		
		Enumeration keys = commentStates.keys();
		while(keys.hasMoreElements())
		{
			
			Double d = (Double)(keys.nextElement());
			String s = (String)(commentStates.get(d));
			//System.out.println("From restore key: "+d);
			//System.out.println("From restore Value: "+s);
			
			input=input.replace(Double.toString(d),parseComment((String)(s)));
			//replaceAll() may treat the random mark in the string as regexp, and therefore treat the decimal dot as any character: Not a big deal, but not what we expect to happen
		}
		
		return input;
	}
	
	public static String parseComment(String input) //Added on 3-19: Since a commented line may be ending with unparsed \r or \n, which has been saved in commentStates
	{
		input=input.replaceAll("\\\\r|\\\\n","\n");
		//System.out.println("Hello:   "+input);
		return input;
	}

	public static boolean hasMemoryAddresses(String input)
	{
		return Pattern.compile(memoryAddressRegexp).matcher(input).find();
	}

	public static String clearLastDoubleQuote(String input) //Clears a possibly existing exessive double quote in SQL that contains memory addresses
	{
		input = input.replaceAll("\"([.][.][.])?+[\n ]*$","");
		input = input.replaceAll("\\\\$","\\\"");
		return input;
		
	}

	public static String replaceAllWithNTimes(String input) //This method replaces the regular expression "'x' <repeats n times>" with n repetitions of character x
	{
		int numOfRepeats = -1;
		Hashtable<String, StringBuffer> theTable = new Hashtable<String, StringBuffer>();
		String letterToRepeat = "";
		StringBuffer replacement= new StringBuffer("");
		String occurrance = "";
		String occurrance2 = "";
		StringBuffer replacement2 = new StringBuffer("");
		//System.out.println(input);
		Matcher m = Pattern.compile(("(\")?(,)?\\s*('(.)'\\s+<repeats\\s+(\\d++)\\s+times>)(,\\s+\")?+")).matcher(input);

		//During the following code, it was not possible to do on the fly replacement inside the first loop which checks for pattern matches and compose the replacement string
		//That would screw it. The only way to do that was to store the occurances of the matches, and the corresponding replacement strings into a Hashtable, and then do the replacements separately in the while next loop

		while(m.find())
		{
			replacement = new StringBuffer("");
			//System.out.println("Hello");
			try
			{
				numOfRepeats = Integer.parseInt(m.group(5));
				//System.out.println(numOfRepeats);
			}
			catch (NumberFormatException nfe)
			{
				throw nfe;
			}
			letterToRepeat = m.group(4);
			//System.out.println(letterToRepeat);
			for (int i=0; i<numOfRepeats; i++)
			{
				replacement.append(letterToRepeat);
			}
			occurrance = input.substring(m.start(),m.end());
			theTable.put(occurrance, replacement);
		}
		Enumeration theTableKeys = theTable.keys();
		while(theTableKeys.hasMoreElements())
		{
			occurrance2 = (String)(theTableKeys.nextElement());
			//System.out.println("Occurrance2: "+ occurrance2);
			replacement2 = (StringBuffer)(theTable.get(occurrance2));
			//System.out.println("replacement2: "+ replacement2);
			input=input.replace(occurrance2, replacement2);
		}
		//System.out.println(input);
		return input;
	}
	
	public static String clearNewLines(String input) //This method is only to be envoked if there are no memory addresses in the SQL
	{
	
		//input = input.replaceAll("(\n)([\n|\r])","$1 $2 ");
		//input = input.replaceAll("\n\z","");
		//String tempInput = "";
		
		//tempInput = input.replaceAll("\n ","<backslashnspace>");
		//System.out.println("Temp Input: "+tempInput);
		//System.out.println("\n\nInput: "+input);
		/*if(tempInput.contains("\n")) //This is a trick to check if there are new lines without spaces (i.e, check if lines are not appended one space position to the right), then add spaces at the left if there is no. This makes it match required format in ordered to be cleaned out properly
		{System.out.println("Inside if");
			input = input.replaceAll("\n","\n ");
		}*/
		//System.out.println("\n\nModified Input: "+input);
		input = input.replaceAll("([\n]?)[ ]?(.{70,}?)\n[ ]?(?=\\S)","$1$2");
		return input;
	}
	
}
