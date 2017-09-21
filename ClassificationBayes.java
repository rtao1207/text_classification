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
 * Description:参考论文：《基于朴素贝叶斯方法的中文文本分类研究》 河北大学 李丹 采用的是多项式模型； 程序的调用方式是：
 * 1.loadLabel(File originalFile) 加载类文档； 2.getTotalTerm(File mapFile) 读取.map文件，获得总词数
 * 3.calByes(File testFile , File indexFile) 计算测试集与特征项索引文档的贝叶斯值； 4.outputResult(File
 * resultFile) 将结果输出 输出的格式是 测试文档本身的type 被分到的类别号
 * 
 * @author Administrator
 * 
 */
public class ClassificationBayes_rt {
	
	List<TestEssayRecord> testList = new ArrayList<TestEssayRecord>(); // 存放所有的测试集最后的结果；
	
	int totalTerm = 0;//总共有多少词
	int type = 0;//类别
	int termNum = 0;//.data文件中每个特征词的编号
	int labelSum;//总类别数
	int totalDocNum = 0; //总文档数
	
	int[] eachTermTotal = null; // 每个类的总的特征词数；
	int[] eachTermFreqTotal = null; // 每个类的总的特征词频数；

	double [] [] term= null;//保存.data文件中数据
	double [] eachtermNum = null;//保存.data文件中的每个类中所含特征数量
	
	private long totalTermFreq;
	private long adjustmentFactor = 1000;
	
	/**
	 * 功能是读取所有类级别的信息，现在的只存储了每个类别总的文章数和总的特征数
	 * 需要的是总的类别数和每个类中的词数之和，即labelSum
	 * @param labelFile
	 */
	public void loadLabel(File labelFile) {
		try {
			FileReader fr = new FileReader(labelFile);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			labelSum = line.split(",").length;//共有多少个类别
			eachTermTotal = new int[labelSum];//每个类中的总的特征词数，即总词数
			eachTermFreqTotal = new int[labelSum];//每个类中特征词的总的出现的次数，即总频数

			int start = 0, end = 0, type;
			String[] lineSplit;
			while ((line = br.readLine()) != null) {
				lineSplit = line.split(" ", 2);
				type = Integer.parseInt(lineSplit[0].trim());
				start = lineSplit[1].trim().indexOf(":");
				end = lineSplit[1].trim().lastIndexOf(":");
				eachTermTotal[type] = Integer.parseInt(lineSplit[1].trim()
						.substring(start + 1, end));//每个类中的总的特征词数，即总词数
				eachTermFreqTotal[type] = Integer.parseInt(lineSplit[1].trim()
						.substring(end + 1));//每个类中特征词的总的出现的次数，即总频数
				
			}
			
			
//			for (int i = 0; i < eachTermTotal.length; i++) {
//				totalTermSum += eachTermTotal[i];
//			}
//			
			br.close();
			System.out.println("总的类别数:" + this.labelSum);
			System.out.println("成功加载类别信息！" + labelFile.getAbsolutePath());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取.map文件，获得总词数
	 * @param mapFile
	 * @return lineNum 总词数
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
		// 获得每个类的概率；p(cj)
		double[] probC = new double[this.labelSum];

		for (int i = 0; i < this.labelSum; i++) {
			totalTermFreq += this.eachTermFreqTotal[i];//文本集中总的特征词频
			probC[i] = (double) eachTermTotal[i] / this.totalTerm;//每个类中的特征词数除以总的特征词数
		}
		System.out.println("总的特征词数：" + this.totalTerm);//1000
		System.out.println("总的特征词频数：" + totalTermFreq);//9654
		long aveTermFreq = totalTermFreq / this.labelSum;//平均每个类的特征词频数
		this.adjustmentFactor = Math.round(Math.pow(10,
				(aveTermFreq + "").length() - 2));//获得调节数
		System.out.println("调节数：" + this.adjustmentFactor);
		
		int totalTermNum = 0;
		String testFileName = testFile.getName();
		String mapFileName = testFileName.substring(0,testFileName.indexOf(".")) +"." + Parameter.mapSuff;
		File mapFile = new File(testFile.getParent(),mapFileName);
		ClassificationBayes_rt by = new ClassificationBayes_rt();
		totalTermNum = by.getTotalTerm(mapFile);
		
		try {
			RandomAccessFile raf = new RandomAccessFile(dataFile, "r");//读取训练集.data文件
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
			
			FileReader fr = new FileReader(testFile);//读取测试集.data文件
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
				type = Integer.parseInt(labelString.toString());//类别信息
				lineNum++;
				TestEssayRecord te = new TestEssayRecord(lineNum, type);//记录文章和类别
				
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
					System.out.println("文章编号：" + lineNum);
					bayesType = 0;
				}else {
					System.out.println("文章编号：" + lineNum);
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
	 * Description:开始输出结果； 输出的格式： 测试文档本身的type 被分到的类别号
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
