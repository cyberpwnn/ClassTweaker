package org.cyberpwn.classtweaker;

public interface IClassTweaker
{
	public byte[] transform(String className, byte[] bytes);
}
