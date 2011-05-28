package $package;

import javax.ejb.Stateless;

@Stateless
public class CalculatorBean implements CalculatorRemote {

	public int add(int input1, int input2) {
		return input1 + input2;
	}

}
