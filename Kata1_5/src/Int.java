
public class Int {
	
	int value;
	
	Int(int value) {
		this.value = value;
	}
	
	int increaseAndGet() {
		return ++value;
	}
	
	int decreaseAndGet() {
		return --value;
	}
	
	int getAndIncrease() {
		return value++;
	}
	
	int getAndDecrease() {
		return value--;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Int) ? ((Int)obj).value == this.value : false; 
	}

}
