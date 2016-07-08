/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.util.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author lb
 *
 */
public abstract class CpuStat {
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static class CpuUsage {
		public static class CoreUsage {
			int coreNumber;
		    int user;
		    int nice;
		    int system;
		    long idle;
		    int iowait;
		    int irq;
		    int softirq;
		    
		    public CoreUsage(int coreNumber, int user, int nice, int system, long idle, int iowait, int irq, int softirq) {
		    	this.coreNumber = coreNumber;
		    	this.user = user;
		    	this.nice = nice;
		    	this.system = system;
		    	this.idle = idle;
		    	this.iowait = iowait;
		    	this.irq = irq;
		    	this.softirq = softirq;
		    }
		    
			public int getCoreNumber() {
				return coreNumber;
			}
			public void setCoreNumber(int coreNumber) {
				this.coreNumber = coreNumber;
			}
			public int getUser() {
				return user;
			}
			public void setUser(int user) {
				this.user = user;
			}
			public int getNice() {
				return nice;
			}
			public void setNice(int nice) {
				this.nice = nice;
			}
			public int getSystem() {
				return system;
			}
			public void setSystem(int system) {
				this.system = system;
			}
			public long getIdle() {
				return idle;
			}
			public void setIdle(long idle) {
				this.idle = idle;
			}
			public int getIowait() {
				return iowait;
			}
			public void setIowait(int iowait) {
				this.iowait = iowait;
			}
			public int getIrq() {
				return irq;
			}
			public void setIrq(int irq) {
				this.irq = irq;
			}
			public int getSoftirq() {
				return softirq;
			}
			public void setSoftirq(int softirq) {
				this.softirq = softirq;
			}

			@Override
			public String toString() {
				return "CoreUsage [coreNumber=" + coreNumber + ", user=" + user
						+ ", nice=" + nice + ", system=" + system + ", idle="
						+ idle + ", iowait=" + iowait + ", irq=" + irq
						+ ", softirq=" + softirq + "]";
			}			
		}

		protected int numCores;
		protected float[] lastCpuUsage;
		protected boolean detectedIdleOrIoWaitDrop = false;
		protected List<CoreUsage> coreUsageList = new ArrayList<>();

		public int getNumCores() {
			return numCores;
		}
		public void setNumCores(int numCores) {
			this.numCores = numCores;
		}
		public List<CoreUsage> getCoreUsageList() {
			return coreUsageList;
		}
		public void setCoreUsageList(List<CoreUsage> coreUsageList) {
			this.coreUsageList = coreUsageList;
		}
		
		public long getIdle(int cpuCore) {
			return coreUsageList.get(cpuCore).getIdle();		
		}
		
		public long getIoWait(int cpuCore) {
			return coreUsageList.get(cpuCore).getIowait();
		}
		
		public long getIoWait() {
			long ioWait = 0;
			for (int i = 0; i < coreUsageList.size(); i++) {
					ioWait += getIoWait(i);
			}
			
			return ioWait;
		}
		
		public long getIdle() {
			long idle = 0;
			for (int i = 0; i < coreUsageList.size(); i++) {
					idle += getIdle(i);
			}
			
			return idle;
		}
		
		public long getCpu(int cpuCore) {
			long cpu = 0;
			
			CoreUsage core = coreUsageList.get(cpuCore);
			cpu += core.getIowait() + core.getIrq() + core.getNice() + core.getSoftirq() + 
						core.getSystem() +	core.getUser();
			return cpu;			
		}
		
		public long getCpu() {
			long cpu = 0;
			for (int i = 0; i < coreUsageList.size(); i++) {
				cpu += getCpu(i);
			}
			return cpu;
		}
		
		/**
		 * needed for arm kernel 3.4 (and maybe more): there is a bug that let the idle and iowait numbers drop sometimes
		 * @param newStat
		 * @return
		 */
		private boolean checkForIdleAndIoWaitTimeBug(CpuUsage newStat) {
			return (getIdle() > newStat.getIdle() || getIoWait() > newStat.getIoWait());
		}
		
		/**
		 * 
		 * @param newStat
		 * @return
		 */
		public float[] updateCpuUsage(CpuUsage newStat) {
			if (checkForIdleAndIoWaitTimeBug(newStat)) {
				detectedIdleOrIoWaitDrop = true;
				System.out.println("idle/iowait drop found...");
				return getLastCpuUsage();
			}
			
			float[] result;
			synchronized (coreUsageList) {
				final int cores = coreUsageList.size();
				final float[] cpu1 = new float[cores];
				final float[] cpu2 = new float[cores];
				final float[] idle1 = new float[cores];
				final float[] idle2 = new float[cores];
				result = new float[cores];
				
				try {
					for (int i = 0; i < coreUsageList.size(); i++) {
						cpu1[i] = getCpu(i);
						idle1[i] = getIdle(i);
						cpu2[i] = newStat.getCpu(i);
						idle2[i] = newStat.getIdle(i);
						result[i] = (float)(cpu2[i] - cpu1[i]) / (float)((cpu2[i] + idle2[i]) - (cpu1[i] + idle1[i]));
						
						/*
						 * workaround for samsung s4 if the usage is lower than 0% or higher than 100%:
						 * 
						 * 
						 * first 3 entries are form nexus 4 (correct values):
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=945816, nice=71593, system=672333, idle=44845472, iowait=144684, irq=28, softirq=12832]]]
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=945844, nice=71593, system=672347, idle=44845756, iowait=144684, irq=28, softirq=12832]]]
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=945794, nice=71593, system=672321, idle=44845179, iowait=144684, irq=28, softirq=12832]]]
						 * 
						 * now samsung s4 (look at the idle and usage difference between 1st and 2nd and 2nd and 3rd (this can't be correct):
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=35407, nice=12255, system=28215, idle=104615, iowait=4085, irq=0, softirq=714]]]
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=35415, nice=12255, system=28240, idle=104510, iowait=4085, irq=0, softirq=714]]]
						 * CpuUsage [numCores=0, coreUsageList=[CoreUsage [coreNumber=0, user=35428, nice=12255, system=28271, idle=104669, iowait=4087, irq=0, softirq=714]]]
						 */
						if (Float.isNaN(result[i]) || result[i] < 0f) {
							result[i] = 0f;
						}
						else if (result[i] > 1f) {
							result[i] = 1f;
						}
					}
				}
				catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
							
				this.coreUsageList.clear();
				this.coreUsageList.addAll(newStat.getCoreUsageList());
				this.numCores = cores;
			}
			lastCpuUsage = result;
			return result;
		}
		
		public boolean isDetectedIdleOrIoWaitDrop() {
			return detectedIdleOrIoWaitDrop;
		}
		
		public float[] getLastCpuUsage() {
			return lastCpuUsage;
		}
		
		@Override
		public String toString() {
			return "CpuUsage [numCores=" + numCores + ", coreUsageList="
					+ coreUsageList + "]";
		}
	}
	
	protected CpuUsage currentCpuUsage;
	
	/**
	 * 
	 * @return
	 */
	public CpuUsage getLastCpuUsage() {
		return currentCpuUsage;
	}
	
	/**
	 * 
	 * @return
	 */
	protected abstract CpuUsage getCurrentCpuUsage(boolean getByCore);
	
	/**
	 * 
	 * @return
	 */
	public float[] update(boolean getByCore) {
		if (currentCpuUsage == null) {
			currentCpuUsage = getCurrentCpuUsage(getByCore);
			return null;
		}
		else {
			return currentCpuUsage.updateCpuUsage(getCurrentCpuUsage(getByCore));
		}
	}
}
