package categorization.bayes;

import global.Parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import categorization.EvaluationIndex;
import categorization.TestEssayRecord;

/**
 * Description:�ο����ģ����������ر�Ҷ˹�����������ı������о��� �ӱ���ѧ � ���õ��Ƕ���ʽģ�ͣ� ����ĵ��÷�ʽ�ǣ�
 * 1.loadLabel(File originalFile) �������ĵ��� 2.getTotalTerm(File mapFile) ��ȡ.map�ļ�������ܴ���
 * 3.calByes(File testFile , File indexFile) ������Լ��������������ĵ��ı�Ҷ˹ֵ�� 4.outputResult(File
 * resultFile) �������� ����ĸ�ʽ�� �����ĵ������type ���ֵ�������
 * 
 * @author Administrator
 * 
 */
public class ClassificationBayes_rt {
	
	List<TestEssayRecord> testList = new ArrayList<TestEssayRecord>(); // ������еĲ��Լ����Ľ����
	
	int totalTerm = 0;//�ܹ��ж��ٴ�
	int type = 0;//���
	int termNum = 0;//.data�ļ���ÿ�������ʵı��
	int labelSum;//�������
	int totalDocNum = 0; //���ĵ���
	
	int[] eachTermTotal = null; // ÿ������ܵ�����������
	int[] eachTermFreqTotal = null; // ÿ������ܵ�������Ƶ����

	double [] [] term= null;//����.data�ļ�������
	double [] eachtermNum = null;//����.data�ļ��е�ÿ������������������
	
	private long totalTermFreq;
	private long adjustmentFactor = 1000;
	
	/**
	 * �����Ƕ�ȡ�����༶�����Ϣ�����ڵ�ֻ�洢��ÿ������ܵ����������ܵ�������
	 * ��Ҫ�����ܵ��������ÿ�����еĴ���֮�ͣ���labelSum
	 * @param labelFile
	 */
	public void loadLabel(File labelFile) {
		try {
			FileReader fr = new FileReader(labelFile);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			labelSum = line.split(",").length;//���ж��ٸ����
			eachTermTotal = new int[labelSum];//ÿ�����е��ܵ��������������ܴ���
			eachTermFreqTotal = new int[labelSum];//ÿ�����������ʵ��ܵĳ��ֵĴ���������Ƶ��

			int start = 0, end = 0, type;
			String[] lineSplit;
			while ((line = br.readLine()) != null) {
				lineSplit = line.split(" ", 2);
				type = Integer.parseInt(lineSplit[0].trim());
				start = lineSplit[1].trim().indexOf(":");
				end = lineSplit[1].trim().lastIndexOf(":");
				eachTermTotal[type] = Integer.parseInt(lineSplit[1].trim()
						.substring(start + 1, end));//ÿ�����е��ܵ��������������ܴ���
				eachTermFreqTotal[type] = Integer.parseInt(lineSplit[1].trim()
						.substring(end + 1));//ÿ�����������ʵ��ܵĳ��ֵĴ���������Ƶ��
				
			}
			
			
//			for (int i = 0; i < eachTermTotal.length; i++) {
//				totalTermSum += eachTermTotal[i];
//			}
//			
			br.close();
			System.out.println("�ܵ������:" + this.labelSum);
			System.out.println("�ɹ����������Ϣ��" + labelFile.getAbsolutePath());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ.map�ļ�������ܴ���
	 * @param mapFile
	 * @return lineNum �ܴ���
	 */
	public int getTotalTerm(File mapFile){
		int lineNum = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(mapFile, "r");
			String line = "";
			
			while((line = raf.readLine()) != null){
				lineNum++;
			}
			
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lineNum;	
	}
	
	public void calByes(File testFile, File dataFile) {

		String dataFileName = dataFile.getName();
		String labelFileName = dataFileName.substring(0,
				dataFileName.indexOf("."))
				+ "." + Parameter.labelSuff;
		String trainMapFileName = dataFileName.substring(0,
				dataFileName.indexOf("."))
				+ "." + Parameter.mapSuff;;
		File labelFile = new File(dataFile.getParent(), labelFileName);
		File trainMapFile = new File(dataFile.getParent(), trainMapFileName);
		this.loadLabel(labelFile);
		int trainTermNum = this.getTotalTerm(trainMapFile);
		this.totalTerm = trainTermNum;

		totalTermFreq = 0;
		// ���ÿ����ĸ��ʣ�p(cj)
		double[] probC = new double[this.labelSum];

		for (int i = 0; i < this.labelSum; i++) {
			totalTermFreq += this.eachTermFreqTotal[i];//�ı������ܵ�������Ƶ
			probC[i] = (double) eachTermTotal[i] / this.totalTerm;//ÿ�����е��������������ܵ���������
		}
		System.out.println("�ܵ�����������" + this.totalTerm);//1000
		System.out.println("�ܵ�������Ƶ����" + totalTermFreq);//9654
		long aveTermFreq = totalTermFreq / this.labelSum;//ƽ��ÿ�����������Ƶ��
		this.adjustmentFactor = Math.round(Math.pow(10,
				(aveTermFreq + "").length() - 2));//��õ�����
		System.out.println("��������" + this.adjustmentFactor);
		
		int totalTermNum = 0;
		String testFileName = testFile.getName();
		String mapFileName = testFileName.substring(0,testFileName.indexOf(".")) +"." + Parameter.mapSuff;
		File mapFile = new File(testFile.getParent(),mapFileName);
		ClassificationBayes_rt by = new ClassificationBayes_rt();
		totalTermNum = by.getTotalTerm(mapFile);
		
		try {
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");//��ȡѵ����.data�ļ�
			String line2 = "";
			String [] lineSplit1 = null;
			String [] termSplit = null;
			
			term = new double[totalTermNum+1][labelSum];
			
			while((line2 = raf.readLine()) != null){
				lineSplit1 = line2.split(" ");
				type = Integer.parseInt(lineSplit1[0]);
				for (int i = 1; i < lineSplit1.length; i++) {
					termSplit = lineSplit1[i].split(":");
					termNum = Integer.parseInt(termSplit[0]);
					term[termNum][type] += Double.parseDouble(termSplit[1]);
				}
			}
			
			this.eachtermNum = new double[this.labelSum];
			
			for (int k = 0; k < this.term.length; k++) {
				
				for (int m = 0; m < eachtermNum.length; m++) {
					
					eachtermNum[m] += term[k][m];
				}
			}
			
			FileReader fr = new FileReader(testFile);//��ȡ���Լ�.data�ļ�
			BufferedReader testbr = new BufferedReader(fr);

			String line = "";
			int lineNum = 0;
			int type = 0;
			StringBuffer labelString = new StringBuffer();
			String[] lineSplit = null;
			while ((line = testbr.readLine()) != null) {
				lineSplit = line.split(" ");
				labelString.append(lineSplit[0]);
				if (!labelString.toString().matches("\\d+")) {
					System.err.println(lineNum + "is wrong!");
					continue;
				}
				type = Integer.parseInt(labelString.toString());//�����Ϣ
				lineNum++;
				TestEssayRecord te = new TestEssayRecord(lineNum, type);//��¼���º����
				
				int bayesType = 0;
				double[] eachProb = null;
				double con1 = 0.0,con2 = 0.0,pk = 0.,c1 = 0.,c2=0.,resultProb = 0.0;
				int testTermNum = 0;
				double eachNum = 0;
				
				eachProb = new double[this.labelSum];
				con1 = Math.log(probC[0]/probC[1]); 
						
				for (int i = 0; i < this.labelSum; i++) {
					for (int j = 1; j < lineSplit.length; j++) {
						String termStr = lineSplit[j].trim().split(":",2)[0];
						testTermNum = Integer.parseInt(termStr);
						eachNum = this.term[testTermNum][i];
						c1 = (double)(eachNum + 1)/eachtermNum[0];
						c2 = (double)(eachNum + 1)/eachtermNum[1];
						con2 += Math.log((1-c1)/(1-c2)); 
						pk = (double)(eachNum + 1)/eachtermNum[i];

						eachProb[i] += (eachNum + 1)*Math.log(pk/(1-pk));
					}
				}

				
				resultProb = Math.abs(eachProb[0]) - Math.abs(eachProb[1]) + con1 + con2;
				
				if (resultProb >= 0) {
					System.out.println("���±�ţ�" + lineNum);
					bayesType = 0;
				}else {
					System.out.println("���±�ţ�" + lineNum);
					bayesType = 1;
				}														
			
				te.setResultType(bayesType);
				testList.add(te);

				labelString = new StringBuffer();
			}
			raf.close();
			testbr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Description:��ʼ�������� ����ĸ�ʽ�� �����ĵ������type ���ֵ�������
	 * 
	 * @param resultFile
	 */
	public void outputResult(File resultFile) {
		try {
			FileWriter fw = new FileWriter(resultFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("label");
			int i = 0;
			while (i < this.labelSum) {
				bw.append(" " + (i++));
			}
			bw.newLine();

			Iterator<TestEssayRecord> it = this.testList.iterator();
			while (it.hasNext()) {
				TestEssayRecord te = it.next();
				bw.append(te.getLabel() + " " + te.getResultType());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String testFileName = "F:\\ruantao\\doc\\rt_old\\01\\tf_testRepre\\DNC_MI_G1000_WP_Ict_test.data";
		String indexFileName = "F:\\ruantao\\doc\\rt_old\\01\\term_Choose\\DNC_MI_G1000_WP_Ict_train.data";

		File testFile = new File(testFileName);
		File indexFile = new File(indexFileName);

		File resultFile = new File(testFile.getParent(), "bayesRes1000.txt");
		File evalFile = new File(testFile.getParent(), "bayesRes1000.res");

		ClassificationBayes_rt cb = new ClassificationBayes_rt();
		cb.calByes(testFile, indexFile);
		cb.outputResult(resultFile);

		EvaluationIndex ec = new EvaluationIndex();
		ec.getData(resultFile);
		ec.parseData(evalFile);
	}


}
