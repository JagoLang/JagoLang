package jago.domain.imports;

public class Import {

    private final String fromPackage;
    private final String importedClass;
    private final boolean isPackageImport;
    public Import(String fromPackage, String importedClass, boolean isPackageImport) {
        this.fromPackage = fromPackage;
        this.importedClass = importedClass;
        this.isPackageImport = isPackageImport;
    }

    public String getFromPackage() {
        return fromPackage;
    }

    public String getImportedClass() {
        return importedClass;
    }

    public boolean isPackageImport() {
        return isPackageImport;
    }
}
