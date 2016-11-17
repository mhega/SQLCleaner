import java.io.*;
public class SQLCleaner
{
	public static void main(String args[])
	{
		String line;
		String input="";
		try
		{
			File inputFile;
			try {
	                	if("-version".equals(args[0]))
				{
					System.out.println("SQLCleaner. version 2.1\n Created by Mohamed Hegazy");
					return;
				}
				inputFile = new File(args[0]);
			}
			catch(ArrayIndexOutOfBoundsException oobe)
			{
				System.out.println("Usage: java -classpath . SQLCleaner [-version] [SQLInputFile]");
				return;
			}
			
	                BufferedReader inputReader = new BufferedReader (new FileReader(inputFile));
			while((line=inputReader.readLine()) !=null)
			{
				input+=line+"\n";
			}
			inputReader.close();
		}
		catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			return;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			return;
		}
		try {
		System.out.println(Replaceable.replace(input));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
