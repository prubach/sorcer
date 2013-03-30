Welcome to FALCON - Federated Algorithmic Metacomputing
by Michael Alger and Dr. Sobolewski

FALCON's domain is on Service-Oriented Programming. It's main objective
is to explore and change the way Service-Oriented programs are created. 
FALCON provides the capability of desiging a SO program with full algorithmic logic, 
which means incoperating loops and condition to the application itself, with full support
to legacy service providers.

A SO application is composed of 1 or more service request on network objects called
service providers. In SORCER, a Service-Oriented program is called a Service Job which 
may be composed of job or an elementary service request called Service Task as defined 
by the composite design pattern. These Service Jobs and Service Tasks all implement the 
Exertion interface. 


Directory Structure
--falcon
	--base (Inteface)
	--core
		---exertion (Implementation of Conditional Interfaces e.g.(IfExertion, WhileExertion)
	--examples (FALCON testing and demonstration package)
	--validation (Thesis demo package)
	*falcon-all-build.xml (builds all the module (examples and validation) under falcon)
	
	
Description
	1) Example Module - The purpose of this module is to test and simulate different logic using
			the IfExertion and the WhileExertion. There is only a single simple provider (fnEval) on this
			demo. The core of the test relies on the different requestor which uses the Conditional Exertion
			(IfExertion and WhileExertion). Under the legacy module are samples how to create Conditional
			Exertion in a different fashion. However, it is recommended to use the later examples for more 
			control on the condition component.
			
	2) Master Theis Demo - This demo tries to simulate what a normal complex Service-Oriented program might
			require. What happens is a user submits a conditional job to the Jobber where it first does a 
			service request on the Initalize-Condition provider to do some calculation. The result value of this 
			calculation will be then the sentinel value for WhileExertion (loops) and IfExertion (branching) in
			this conditional job. The sentinel value is then use to determine either to call the Derivative-Evaluator
			or the Integral-Evaluator. The WhileExertion is then called to determine the precision of the derivative/integral
			accuracy of the calculation based on the formula. After all the calculation is done, only then the
			Exertion is submitted back to the requestor or the client. So during all the execution of the SO program,
			no where it needs return to the client for a response to determine its control structure.
	
	
Running Demos
	1) Examples Module - Testing and Example Demo
		a) Start the provider under "examples/provider/fnEval/bin"
		b) Select any of the requestor under "examples/requestor/fnEval/**/bin"
		
	2) Validation Module - Master Thesis Demo
		Providers:
			a) Start SORCER-Jobber
			b) Start Derivative-Evaluator under "validation/differentiation/provider/bin"
			c) Start Function-Evaluator under "validation/functionEvaluation/provider/bin"
			d) Start Initialize-Condition under "validation/initializeCondition/provider/bin"
			e) Start Integral-Evaluator under "validation/integration/provider/bin"
			
		Requestor:
			a) Start the conditional Job requestor under "validation/conditionalJob/requestor/bin"