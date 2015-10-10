package uk.ac.cam.ga354.fjava.tick0.test;

import static org.junit.Assert.*;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.ac.cam.ga354.fjava.tick0.ExternalSort;

@RunWith(JUnit4.class)
public class TestSuite {
	
	private static final String FILE_A_LOCATION = "/Users/georgeash/Documents/java/External Sort/src/uk/ac/cam/ga354/fjava/tick0/test/someFileA6.dat";
	private static final String FILE_B_LOCATION = "/Users/georgeash/Documents/java/External Sort/src/uk/ac/cam/ga354/fjava/tick0/test/someFileB6.dat";
	RandomAccessFile subsorted1;
	RandomAccessFile someFileA;	
	@Before
	public void configure() throws IOException{
		//subsorted File
		subsorted1 = new RandomAccessFile("subsortedfile.dat", "rw");
		subsorted1.seek(0);
		//0-3 will be sorted, 4-7 will also be sorted 
		subsorted1.writeInt(1);
		subsorted1.writeInt(5);
		subsorted1.writeInt(6);
		subsorted1.writeInt(9);
		//2nd part
		subsorted1.writeInt(1);
		subsorted1.writeInt(3);
		subsorted1.writeInt(9);
		subsorted1.writeInt(1000);
		subsorted1.seek(0);
		subsorted1.close();
		
		//totally random File.
		someFileA = new RandomAccessFile(FILE_A_LOCATION, "rw");
		someFileA.seek(0);
		someFileA.writeInt(1000000);
		someFileA.writeInt(12);
		someFileA.writeInt(2);
		someFileA.writeInt(1);
		someFileA.writeInt(6);
		someFileA.writeInt(3);
		someFileA.writeInt(1);
		someFileA.writeInt(0);
		someFileA.writeInt(3);
		someFileA.writeInt(12);
		someFileA.writeInt(2);
		someFileA.writeInt(1);
		someFileA.writeInt(6);
		someFileA.writeInt(3);
		someFileA.writeInt(1);
		someFileA.writeInt(0);
		someFileA.close();
	}
//	
//	@Test
	public void RunTests() throws FileNotFoundException, IOException{
		String prefix = "/Users/georgeash/Downloads/test-suite/test";
		for (int i = 0; i<18; i++){
			System.out.println(i + " Size:");
			ExternalSort.sort(prefix + i + "a.dat",prefix + i + "b.dat");
			System.out.println(i + " Checksum:");
			System.out.println(ExternalSort.checkSum(prefix + i + "a.dat"));
		}
	}
	
	@Test
	public void mergeTest() throws IOException{
		RandomAccessFile a1 = new RandomAccessFile("subsortedfile.dat", "rw");
		a1.seek(0);
		RandomAccessFile a2 = new RandomAccessFile("subsortedfile.dat", "rw");
		a2.seek(4*4);
		RandomAccessFile b = new RandomAccessFile("tempfile1.dat", "rw");
		ExternalSort.mergeBlocks(a1, a2, 8*4, b, 4);
		b.seek(0);
		try{
			while(true){
				int currentInt = b.readInt();
				System.out.println(currentInt);
			}
		}
		catch(EOFException e){
			
		}
	}
	
	@Test
	public void sortTest() throws FileNotFoundException, IOException{
		ExternalSort.sort(FILE_A_LOCATION, FILE_B_LOCATION);
		RandomAccessFile fileB = new RandomAccessFile(FILE_B_LOCATION, "r");
		System.out.println("file B:");
		try{
			while(true){
				int currentInt = fileB.readInt();
				System.out.println(currentInt);
			}
		}
		catch(EOFException e){
			
		}
		someFileA = new RandomAccessFile(FILE_A_LOCATION, "r");
		System.out.println("file A:");
		try{
			while(true){
				int currentInt = someFileA.readInt();
				System.out.println(currentInt);
			}
		}
		catch(EOFException e){
			
		}
	}
}
