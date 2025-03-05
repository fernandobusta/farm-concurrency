public class Field {
    // Maybe inherit field class from another class (or interface) that has locks
    // or sempahores already implemented
    private int animalCount;
    private String name;

    public String getName() {
        return this.name;
    }
    public int getAnimalCount() {
        return this.animalCount;
    }
    public void setAnimalCount(int newAnimalCount) {
        this.animalCount = newAnimalCount;
    }

    // These two should be modified and maybe use an already implemented method
    public void lock() {}
    public void unlock() {}
}
