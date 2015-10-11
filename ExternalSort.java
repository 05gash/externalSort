package uk.ac.cam.ga354.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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

	public static void sort(String f1, String f2) throws FileNotFoundException,
			IOException {
		RandomAccessFile fileA1 = new RandomAccessFile(f1, "rw");
		RandomAccessFile fileA2 = new RandomAccessFile(f1, "rw");
		RandomAccessFile fileB1 = new RandomAccessFile(f2, "rw");
		RandomAccessFile fileB2 = new RandomAccessFile(f2, "rw");
		DataOutputStream b1Out = null;
		DataInputStream b1In = null;
		DataInputStream b2In = null;
		DataOutputStream a1Out = null;
		DataInputStream a1In = null;
		DataInputStream a2In = null;

		long k = 1; //size of blocks(no of integers)
		long m = fileA1.length()/4; //no of blocks?
		long numInts = m;

		System.out.println(m);
		boolean readingFromA = true;
		while(k < numInts){
			
			if(readingFromA){
				fileB1.seek(0);
				fileA1.seek(0);
				a1In = new DataInputStream(
					    new BufferedInputStream(new FileInputStream(fileA1.getFD())));
				fileA2.seek(k*4);
				a2In = new DataInputStream(
					    new BufferedInputStream(new FileInputStream(fileA2.getFD())));
				b1Out = new DataOutputStream(
					    new BufferedOutputStream(new FileOutputStream(fileB1.getFD())));
			}
			else{
				fileA1.seek(0);
				fileB1.seek(0);
				b1In = new DataInputStream(
					    new BufferedInputStream(new FileInputStream(fileB1.getFD())));
				fileB2.seek(k*4);
				b2In = new DataInputStream(
					    new BufferedInputStream(new FileInputStream(fileB2.getFD())));
				a1Out = new DataOutputStream(
					    new BufferedOutputStream(new FileOutputStream(fileA1.getFD())));
			}
			
			//debug
//			System.out.println("reading from A " + readingFromA);
//			try{
//				fileA1.seek(0); 
//				fileB1.seek(0);
//				while(true){
//					int currentInt = readingFromA ? fileA1.readInt() : fileB1.readInt();
//					System.out.println(currentInt);
//				}
//			}
//			
//			catch(EOFException e){
//				
//			}
			//end debug
			long i = 0;
			while(i<m){
				if(readingFromA){
					//we see if the first blocksize will underfill
					if((i+1)*k>=numInts){
						//it does, we copy these elements into B.
						copyTo(a1In, numInts-i*k, b1Out);
						i++;
					}//the 2nd blocksize will underfill,
					else if ((i+2)*k>numInts){
						i++;
						mergeBlocks(a1In, a2In, (i-1)*k*4, i*k*4,  numInts*4, b1Out, k);
						i++;
					}
					else{//normal behavior
						i++;
						mergeBlocks(a1In, a2In, (i-1)*k*4, i*k*4, (i+1)*k*4, b1Out, k);
						a1In.skip(k*4);
						a2In.skip(k*4);
						i++;
					}
				}
				else{
					//we see if the first blocksize will underfill
					if((i+1)*k>=numInts){
						//it does, we copy these elements into B.
						copyTo(b1In, numInts-i*k, a1Out);
						i++;
					}//the 2nd blocksize will underfill,
					else if ((i+2)*k>numInts){
						i++;
						mergeBlocks(b1In, b2In, (i-1)*k*4, i*k*4, numInts*4, a1Out, k);
						i++;
					}
					else{//normal behavior
						i++;
						mergeBlocks(b1In, b2In, (i-1)*k*4, i*k*4, (i+1)*k*4, a1Out, k);
						b1In.skip(k*4);
						b2In.skip(k*4);
						i++;
					}	
				}
			}
			m=(m+1)/2;
			k=k*2;
			readingFromA = !readingFromA;
		}
		if(!readingFromA){
			fileB1.seek(0);
			b1In = new DataInputStream(
				    new BufferedInputStream(new FileInputStream(fileB1.getFD())));
			fileA1.seek(0);
			a1Out = new DataOutputStream(
				    new BufferedOutputStream(new FileOutputStream(fileA1.getFD())));
			copyTo(b1In, numInts, a1Out);
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
	
	public static void mergeBlocks(DataInputStream A1In, DataInputStream A2In,long a1Ptr, long a2Ptr, long a2BlockEnd, DataOutputStream BOut, long k2) throws IOException{
		
		long a1BlockEnd = a1Ptr + k2*4;
		a1Ptr +=4;
		a2Ptr +=4;
		int currentIntA1 = A1In.readInt(); 
		int currentIntA2 = A2In.readInt(); 

		while(true){
//			if (a1Ptr >= a1BlockEnd){ //if we are at the end of block 1
//				while(a2Ptr <a2BlockEnd){ //we copy the rest of the contents of block 2 into B, inserting the 
//					int nextIntA2 = A2In.readInt();
//					if(currentIntA1<=currentIntA1 && currentIntA1 < nextIntA2){
//						BOut.writeInt(previous);
//					}
//					BOut.writeInt(nextIntA2);
//					a2Ptr+=4;
//				}
//				break;
//			}
//			else if (a2Ptr >= a2BlockEnd){ //if we are at the end of block 2
//				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA1 : currentIntA2);
//				BOut.writeInt(currentIntA1 <= currentIntA2 ? currentIntA2 : currentIntA2);
//				while(a1Ptr < a1BlockEnd){ //we copy the rest of the contents of block 1 into B
//					BOut.writeInt(A1In.readInt());
//					a1Ptr+=4;
//				}
//				break;
//			}
//			else{ //we merge
			if(a1Ptr<=a1BlockEnd && a2Ptr<= a2BlockEnd){
				if(currentIntA1<=currentIntA2){
					BOut.writeInt(currentIntA1);
					if(a1Ptr==a1BlockEnd){
						currentIntA1 = 0;
					}
					else{
						currentIntA1 = A1In.readInt();
					}
					a1Ptr+=4;
				}
				else if(currentIntA1>currentIntA2){
					BOut.writeInt(currentIntA2);
					if(a2Ptr==a2BlockEnd){
						currentIntA2 = 0;
					}
					else{
						currentIntA2 = A2In.readInt();
					}
					a2Ptr+=4;
				}
			}
			if(a1Ptr > a1BlockEnd){
				BOut.writeInt(currentIntA2);
				while(a2Ptr<a2BlockEnd){
					BOut.writeInt(A2In.readInt());
					a2Ptr+=4;
				}
				break;
			}
			else if(a2Ptr >a2BlockEnd){
				BOut.writeInt(currentIntA1);
				while(a1Ptr<a1BlockEnd){
					BOut.writeInt(A1In.readInt());
					a1Ptr+=4;
				}
				break;
			}
		}
		BOut.flush();
	}
	
	
	public static void copyTo(DataInputStream a1In, long noElements, DataOutputStream bOut) throws IOException{
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
	
	//merges
	public static void mergeFile(RandomAccessFile a1, RandomAccessFile a2, RandomAccessFile b1, long blockSize) throws IOException{
		DataOutputStream BOut = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(b1.getFD())));
		DataInputStream A1In = new DataInputStream(new BufferedInputStream(
				new FileInputStream(a1.getFD())));
		DataInputStream A2In = new DataInputStream(new BufferedInputStream(
				new FileInputStream(a2.getFD())));
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		sort(f1, f2);
		System.out.println("The checksum is: " + checkSum(f1));
	}
}