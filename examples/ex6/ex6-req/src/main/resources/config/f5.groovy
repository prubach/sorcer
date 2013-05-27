package config

import sorcer.arithmetic.provider.Adder;

Task f5 = task("f5", op("add", Adder.class),
   context("add", input(path("arg", "x1"), 20.0d), input(path("arg", "x2"), 80.0d),
	  output(path("result", "y"), null)));
