package org.superbiz;

import javax.ejb.Remote;

@Remote
public interface CalculatorRemote {
	public int add(int input1, int input2);
}
