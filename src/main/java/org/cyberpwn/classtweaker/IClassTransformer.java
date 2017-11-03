package org.cyberpwn.classtweaker;

public interface IClassTransformer
{
	public byte[] transform(String className, byte[] bytes);
}
