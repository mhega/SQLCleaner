import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

public class DumpSQLCleaner extends JFrame
{
	public static void main(String args[])
	{
		DumpSQLCleaner dsc = new DumpSQLCleaner("SQLCleaner");
	}

	public DumpSQLCleaner(String name)
	{
		super(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800,600);
		this.assemble();
		this.setVisible(true);
	}

	private JMenuBar mainMenu = new JMenuBar();
	private JMenu file = new JMenu("File");
	private JMenu edit = new JMenu("Edit");
	private JMenuItem exit = new JMenuItem("Exit");
	private JMenuItem about = new JMenuItem("About");
	private JScrollPane scrollPane;
	private JTextArea sql = new JTextArea();
	private JDialog aboutDialog;
	private JLabel aboutLabel;
	private JPanel aboutOkPanel = new JPanel();;
	private JButton aboutOkButton = new JButton("OK");
	private JMenuItem copy = new JMenuItem("Copy");
	private JMenuItem paste = new JMenuItem("Paste");
	
	/*private final JTextComponent.KeyBinding[] defaultBindings = {
     		new JTextComponent.KeyBinding(
       		KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
       		DefaultEditorKit.copyAction),
     		new JTextComponent.KeyBinding(
       		KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
       		DefaultEditorKit.cutAction)
   	};*/

 	private String getClipboard()
	{
    		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    		try 
		{
        		if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) 
			{
            			String text = (String)t.getTransferData(DataFlavor.stringFlavor);
            			return text;
        		}
    		}
		catch (UnsupportedFlavorException ufe)
		{ufe.printStackTrace();}
		catch (IOException ioe) 
		{ioe.printStackTrace();}
    		return null;
	}
		
	public void assemble()
	{	
		String aboutText="<html><div align='CENTER'>SQL Cleaner&nbsp;&nbsp;&nbsp;2.1C"+
		"<br>Cleans SQL extracted from Teradata Database dumps<br>Created by Mohamed Hegazy</div></html>";
		aboutDialog = new JDialog(this,"SQL Cleaner");
		aboutDialog.setModal(true);
		aboutLabel = new JLabel();
		//aboutLabelSouth = new JLabel(this,"<html>1.0<br></html>")
		aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
		aboutLabel.setVerticalAlignment(SwingConstants.CENTER);
		aboutLabel.setText(aboutText);
		aboutDialog.add(aboutLabel);
		aboutOkButton.setMaximumSize(new Dimension(25,25));
		//aboutOkButton.setPreferredSize(new Dimension(50,10));
		aboutOkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		aboutOkPanel.add(aboutOkButton);
		aboutDialog.add(aboutOkPanel,BorderLayout.SOUTH);
		aboutDialog.setSize(300,200);
		scrollPane = new JScrollPane(sql);
		file.add(about);
		file.add(exit);
		edit.add(copy);
		edit.add(paste);
		file.setMnemonic(KeyEvent.VK_F);
		edit.setMnemonic(KeyEvent.VK_E);
		about.setMnemonic(KeyEvent.VK_A);
		copy.setMnemonic(KeyEvent.VK_C);
		paste.setMnemonic(KeyEvent.VK_P);
		mainMenu.add(file);
		mainMenu.add(edit);

		//sql.setKeymap(null);
		sql.setEditable(false);
		sql.setText("Please use CTRL+V to paste SQL..");

		sql.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{
				String clipBoardContents;
				try
				{
					if (ke.getKeyCode() == KeyEvent.VK_V && ke.isControlDown())
					{
						clipBoardContents = DumpSQLCleaner.this.getClipboard();
						if (clipBoardContents ==null)
							JOptionPane.showMessageDialog(DumpSQLCleaner.this,"Clipboard empty, or invalid","Error",JOptionPane.ERROR_MESSAGE);
						else
							DumpSQLCleaner.this.sql.setText(Replaceable.replace(clipBoardContents));
						//DumpSQLCleaner.this.sql.setText("Hello..."+DumpSQLCleaner.this.getClipboard()); //Put whatever here
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(DumpSQLCleaner.this,"Error Occurred! Please consult the developer","Error",JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				System.exit(0);
			}
		});
		
		about.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				aboutDialog.setVisible(true);
			}
		});

		copy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				DumpSQLCleaner.this.sql.selectAll();
				DumpSQLCleaner.this.sql.copy();
			}
		});



		paste.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				//DumpSQLCleaner.this.sql.selectAll();
				String clipBoardContents = DumpSQLCleaner.this.getClipboard();
				try
				{
					if (clipBoardContents ==null)
						JOptionPane.showMessageDialog(DumpSQLCleaner.this,"Clipboard empty, or invalid","Error",JOptionPane.ERROR_MESSAGE);
					else
						DumpSQLCleaner.this.sql.setText(Replaceable.replace(clipBoardContents));
					//DumpSQLCleaner.this.sql.setText("Hello..."+DumpSQLCleaner.this.getClipboard()); //Put whatever here
				}
				catch(Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(DumpSQLCleaner.this,"Error Occurred! Please consult the developer","Error",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		aboutOkButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				aboutDialog.dispose();
			}
		});
		
		this.setLayout(new BorderLayout());
		Container contentPane = this.getContentPane();
		contentPane.add(mainMenu, BorderLayout.NORTH);
		contentPane.add(scrollPane, BorderLayout.CENTER);
	}

	/*private String cleanSQL(String)
	{
		return Replaceable.replace(String);
	}*/
}