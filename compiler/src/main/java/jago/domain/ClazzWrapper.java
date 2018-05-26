package jago.domain;

public class ClazzWrapper {
    private String fileName;
    private byte[] bytecode;

    public ClazzWrapper(String fileName, byte[] bytecode) {
        this.fileName = fileName;
        this.bytecode = bytecode;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getBytecode() {
        return bytecode;
    }
}
