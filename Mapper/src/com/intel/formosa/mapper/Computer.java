package com.intel.formosa.mapper;


public class Computer {
	private String ip;
	private String taskSessionId;
    private String cpu;
    private String mem;

    private long cpuIdle = 0;
    private long cpuTotal = 0;
    private long memAvailable = 0;
    private long memFree = 0;
	
	public Computer(String ip) {
		this.ip = ip;
	}

    /** Getter */
	public String getIp () {
		return ip;
	}

    public String getCpu () {
        return cpu;
    }

    public String getMem () {
        return mem;
    }

    public long getCpuIdle () {
        return cpuIdle;
    }
    public long getMemAvailable () {
        return memAvailable;
    }

    public long getMemFree () {
        return memFree;
    }
	
	public String getTaskSessionId () {
		return taskSessionId;
	}

    /** Setter */
	public void setTaskSessionId (String sessionId) {
		taskSessionId = sessionId;
	}

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public void setMem (String mem) {
        this.mem = mem;
    }

    public void setCpuIdle(long cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public void setCpuTotal(long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public void setMemAvailable (long memAvailable) {
        this.memAvailable = memAvailable;
    }

    public void setMemFree (long memFree) {
        this.memFree = memFree;
    }
}
