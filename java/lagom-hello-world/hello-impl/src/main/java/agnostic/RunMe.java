package agnostic;

import name.jchein.demo.lagom.hello_world.hello.api.HelloEvent;
//import name.jchein.demo.lagom.hello_world.hello.api.HelloEvent.GreetingMessageChanged;

public class RunMe {

	public static void main(String[] args) {
		HelloEvent.GreetingMessageChanged greet = 
			new HelloEvent.GreetingMessageChanged("name", "message");
		HelloEvent hello = greet;
		
		System.out.println(greet.getMessage());
		System.out.println(greet.getName());
		System.out.println(hello.getName());
	}

}
