package pojos;

import java.io.Serializable;


public class ProgramPOJO implements Serializable {
    private int memorySize;

    public ProgramPOJO(int memorySize) {
        this.memorySize = memorySize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }
}
