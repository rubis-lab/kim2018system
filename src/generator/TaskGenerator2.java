package generator;

import period_harmonizer.PeriodModifier;
import multicore_exp.Param;
import multicore_exp.Util;
import data.Task;
import data.TaskSet;

public class TaskGenerator2 extends TaskGenerator {
	protected PeriodModifier periodModifier;
	
	public TaskGenerator2(PeriodModifier periodModifier)
	{
		super();
		this.periodModifier = periodModifier;
	}
	

	public Task GenerateTask(double alpha, double beta, double gamma, int taskID, int seed)
	{
		boolean allOptionsSameIntermediateDeadline = true;
		Util util = new Util(seed);
		int numSegments = util.randomInt(kmin, kmax); 	
		
		int sumOneThreads = 0;
		Task task = new Task(numSegments);
		
		int period = util.randomInt((int)Param.Period_MIN, (int)Param.Period_MAX);
		period = periodModifier.modifyPeriod(period);
		
//		gamma = util.randomDoubleNormalDistribution(gamma, 0.1);
		if (gamma > 1) gamma = 1;
		if (gamma <= 0) gamma = 0.1;
		int deadline = (int) (period * gamma);
		
//		beta = util.randomDoubleNormalDistribution(beta, 0.1);
		if (beta <= 0) beta = 0.1;
		if (beta > Param.NumProcessors) beta = Param.NumProcessors;
		int totalExecutionTime = (int) (period * beta);
		
		task.setDeadline(deadline);
		task.setRealDeadline(deadline);
		task.setPeriod(period);
		task.setRealPeriod(period);
		
		int[] fractionalExecutionTime = new int[numSegments];
		int fractionalExecutionTimeSum = 0;
		for (int i = 0; i < numSegments; i++)
		{
			fractionalExecutionTime[i] = util.randomInt(scmin, scmax);
			fractionalExecutionTimeSum += fractionalExecutionTime[i];
		}
		
		for (int j = 0; j < numSegments; j++)
		{
			int wcetOneThread = (int)
					(fractionalExecutionTime[j]  / (double)fractionalExecutionTimeSum * totalExecutionTime);
			for(int k = 0; k<omax; k++)
			{
				int Oik = k + 1;
				int wcetEachThread = (int)Math.ceil((alpha * wcetOneThread) +( (1 - alpha) * wcetOneThread / Oik));
				if(wcetEachThread == 0) wcetEachThread = 1;
				
				for(int x = 0; x < Oik ; x++)
					task.setExecutionTime(j,  k,  x, wcetEachThread);
			}
		}
		sumOneThreads = totalExecutionTime;
		
		
		
		if(allOptionsSameIntermediateDeadline == true)
		{
			for(int i= 0 ; i < task.getNumSegments() ; i++)
			{
				task.setIntermediateDeadline(i, (int) (sumOneThreads / beta));
			}
		}
		
		
		task.setTaskID(taskID);
		task.setInformation(seed, scmin, scmax, omax, kmin, kmax);
		
		return task;
		
	}
}
