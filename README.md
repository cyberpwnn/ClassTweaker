# ClassTweaker
Used in plugins to tweak/modify server and other plugin classes

# Using ASM 5.2 in your plugins
To use asm, you will need to set up a few things, in this example, maven will be used.

### 1. Add ClassTweaker to your pom
``` xml
<repositories>
    <repository>
        <id>volmit</id>
        <url>http://nexus.volmit.com/content/repositories/volmit</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.cyberpwn</groupId>
        <artifactId>ClassTweaker</artifactId>
        <version>1.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 2. Define asm.json
The asm.json file goes right with your plugin.yml (resource). **Be sure to add this to your resource filtering if you use it**

``` json
{
  "transformers": [
    {
      "version": "v1_12_R1",
      "class": "com.you.SimpleTransformer12R1",
      "includes": [
        "your-group-name"
      ]
    }
  ],
  "groups": {
    "your-group-name": {
      "classes": [
        "net.minecraft.server.v1_12_R1.EntityPig",
        "net.minecraft.server.v1_12_R1.EntityCow",
        "net.minecraft.server.v1_12_R1.EntitySheep",
      ]
    }
  }
}
```

You can also define multiple versions, for example if you know all class versions will work the same

```
{
  "transformers": [
    {
      "versions": [
        "v1_12_R1",
        "v1_11_R1",
        "v1_10_R1",
        "v1_9_R2",
        "v1_9_R1"
      ],
      "class": "com.example.transformers.PigTemptationTransformer",
      "includes": [
        "pigs"
      ]
    },
  ],
  "groups": {
    "pigs": {
      "classes": [
        "net.minecraft.server.v1_12_R1.EntityPig",
        "net.minecraft.server.v1_11_R1.EntityPig",
        "net.minecraft.server.v1_10_R1.EntityPig",
        "net.minecraft.server.v1_9_R2.EntityPig",
        "net.minecraft.server.v1_9_R1.EntityPig"
      ]
    }
  }
}
```

##### Transformers 
1. Define transformers. A transformer transforms classes that are fed to it by the launch wrapper
2. Each transformer needs a version. This version is the package version, not the game version
3. Each transformer needs a class declaration. This specifies where the transformer is in your plugin to be loaded up to transform specified classes
4. Each transformer needs "includes" which is a list of groups defined in the groups section. Each group can contain as many classes as you like. Any classes that are not present are simply ignored.

##### Groups
1. Define groups. A group is a collection of classes that are included in any transformer.
2. Groups can have multiple versions of the same class (v1_12_R1 & v1_11_R1 for example), The launch wrapper will only give transformers the version they are looking for, and will never include classes that are not present at runtime.
3. Multiple groups can define the same class

### 3. Define Your Transformer(s)
``` java
package com.you;

import org.cyberpwn.classtweaker.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

// Must implement IClassTransformer (from ClassTweaker)
public class SimpleTransformer12R1 implements IClassTransformer
{
	@Override
	public byte[] transform(String className, byte[] bytes)
	{
    		// If your transformer has multiple classes defined, or groups,
   		// It is wise to check what class you are transforming
		if(className.equals("net.minecraft.server.v1_12_R1.EntityPig"))
		{
      			// In this example, we are simply modifying one of the pigs goals
      			// to "tempt" pigs due to diamonds being held instead of carrots on a stick
			String itemsClass = "net/minecraft/server/v1_12_R1/Items";
			String itemsDesc = "Lnet/minecraft/server/v1_12_R1/Item;";
			String itemsName = "CARROT_ON_A_STICK";
			String itemsExtraName = "DIAMOND";

			ClassNode node = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(node, ClassReader.EXPAND_FRAMES);

			for(Object i : node.methods)
			{
				MethodNode method = (MethodNode) i;

				if(method.name.equals("r"))
				{
					for(AbstractInsnNode j : method.instructions.toArray())
					{
						if(j.getOpcode() == Opcodes.GETSTATIC)
						{
							FieldInsnNode finsnNode = (FieldInsnNode) j;

							if(finsnNode.owner.equals(itemsClass) 
							&& finsnNode.name.equals(itemsName) 
							&& finsnNode.desc.equals(itemsDesc))
							{
								finsnNode.name = itemsExtraName;
								break;
							}
						}
					}
				}
			}

     			// Simple ASM stuff here.
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			node.accept(cw);

			return cw.toByteArray();
		}

		return bytes;
	}
}

```
