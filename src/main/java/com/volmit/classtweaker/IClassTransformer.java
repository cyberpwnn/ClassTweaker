package com.volmit.classtweaker;

public interface IClassTransformer
{
	public byte[] transform(String className, byte[] bytes);
}
