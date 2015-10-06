package uk.ac.cam.ga354.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExternalSort {
	
	//we dont work in bytes instead we work in number of integers.

	static private long k;

	public static void sort(String f1, String f2) throws FileNotFoundException,
			IOException {
		RandomAccessFile fileA1 = new RandomAccessFile(f1, "rw");
		RandomAccessFile fileA2 = new RandomAccessFile(f1, "rw");
		RandomAccessFile fileB1 = new RandomAccessFile(f2, "rw");
		RandomAccessFile fileB2 = new RandomAccessFile(f2, "rw");

		long k = 1; //size of blocks(no of integers)
		long m = fileA1.length()/4; //no of blocks?
		long numInts = m;

		System.out.println(m);
		boolean readingFromA = true;
		while(k < numInts){
			if(readingFromA){
				fileB1.seek(0);
			}
			else{
				fileA1.seek(0);
			}
			long i = 0;
			while(i<m){
				if(readingFromA){
					//we see if the first blocksize will underfill
					if((i+1)*k>=numInts){
						//it does, we copy these elements into B.
						fileA1.seek(i*k*4);
						copyTo(fileA1, numInts-i*k, fileB1);
						i++;
					}//the 2nd blocksize will underfill,
					else if ((i+2)*k>numInts){
						fileA1.seek(i*k*4);
						i++;
						fileA2.seek(i*k*4);
						mergeBlocks(fileA1, fileA2, numInts, fileB1, k);
						i++;
					}
					else{//normal behavior
						fileA1.seek(i*k*4);
						i++;
						fileA2.seek(i*k*4);
						mergeBlocks(fileA1, fileA2, (i+1)*k*4, fileB1, k);
						i++;
					}
				}
				else{
					//we see if the first blocksize will underfill
					if((i+1)*k>=numInts){
						//it does, we copy these elements into B.
						copyTo(fileB1, numInts-i*k, fileA1);
						i++;
					}//the 2nd blocksize will underfill,
					else if ((i+2)*k>numInts){
						fileB1.seek(i*k*4);
						i++;
						fileB2.seek(i*k*4);
						mergeBlocks(fileB1, fileB2, numInts, fileA1, k);
						i++;
					}
					else{//normal behavior
						fileB1.seek(i*k*4);
						i++;
						fileB2.seek(i*k*4);
						mergeBlocks(fileB1, fileB2, (i+1)*k*4, fileA1, k);
						i++;
					}	
				}
			}
			m=(m+1)/2;
			k=k*2;
			readingFromA = !readingFromA;
		}
		fileA1.close();
		fileA2.close();
		fileB1.close();
		fileB2.close();
	}

	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}
	
	public static void mergeBlocks(RandomAccessFile a1,RandomAccessFile a2, long a2BlockEnd, RandomAccessFile b, long k2) throws IOException{
		DataOutputStream BOut = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(b.getFD())));
		DataInputStream A1In = new DataInputStream(new BufferedInputStream(
				new FileInputStream(a1.getFD())));
		DataInputStream A2In = new DataInputStream(new BufferedInputStream(
				new FileInputStream(a2.getFD())));
		long a1BlockEnd = a1.getFilePointer() + k2*4;
		int currentIntA1 = a1.readInt(); 
		int currentIntA2 = a2.readInt(); 
		long a1Ptr = a1.getFilePointer();
		long a2Ptr = a2.getFilePointer();

		while(true){
			System.out.println("a1 filepointer: " + a1Ptr + ", a2 filepointer:" + a2Ptr);
			if (a1Ptr >= a1BlockEnd){ //if we are at the end of block 1
				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA1 : currentIntA2);
				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA2 : currentIntA1);

				while(a2Ptr <a2BlockEnd){ //we copy the rest of the contents of block 2 into B
					BOut.writeInt(A2In.readInt());
					a2Ptr+=4;
				}
				break;
			}
			else if (a2Ptr >= a2BlockEnd){ //if we are at the end of block 2
				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA1 : currentIntA2);
				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA1 : currentIntA2);
				while(a1Ptr < a1BlockEnd){ //we copy the rest of the contents of block 1 into B
					BOut.writeInt(A1In.readInt());
					a1Ptr+=4;
				}
				break;
			}
			else{ //we merge
				if(currentIntA1<=currentIntA2){
					BOut.writeInt(currentIntA1);
					currentIntA1 = A1In.readInt();
					a1Ptr+=4;
				}
				else if(currentIntA1 > currentIntA2){
					BOut.writeInt(currentIntA2);
					currentIntA2 = A2In.readInt();
					a2Ptr+=4;
				}
			}
		}
		BOut.flush();
	}
	
	public static void copyTo(RandomAccessFile a1, long noElements, RandomAccessFile b1) throws IOException{
		DataOutputStream bOut = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(b1.getFD())));
		DataInputStream a1In = new DataInputStream(new BufferedInputStream(
				new FileInputStream(a1.getFD())));
		for(int i=0; i<noElements; i++){
			bOut.writeInt(a1In.readInt());
		}
		bOut.flush();
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for (byte v : md.digest())
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		sort(f1, f2);
		System.out.println("The checksum is: " + checkSum(f1));
	}
}