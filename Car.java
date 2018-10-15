import java.util.HashSet;
import java.util.Set;

public class Car implements CarElement{
    private String name;
    private Set<CarElement> elements = new HashSet<>();
    public Car(String n){
        name = n;
        for(int i=1; i<5; i++)
            elements.add(new Wheel());
    }
    public void accept(CarElementVisitor visitor){
        for(CarElement element:elements)
            element.accept(visitor);
        visitor.visit(this);
    }

    public String getName() {
        return name;
    }
    public int getWheelCount(){
        int count = 0;
        for(CarElement element: elements)
            if(element instanceof Wheel)
                count++;
        return count;
    }
    public static void main(String[] args) {
        Car c =new Car("a");
        CarElementVisitor v = new CarElementVisitorPrint();
        c.accept(v);
        }
}

