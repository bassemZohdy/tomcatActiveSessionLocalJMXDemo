package demo;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Value("${managment.agent.jar}")
	String managmentAgentJar;
	@Value("${application.name}")
	String applicationName;
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		final AttachProvider attachProvider = AttachProvider.providers().get(0);

		VirtualMachineDescriptor descriptor = null;
		for (VirtualMachineDescriptor virtualMachineDescriptor : attachProvider
				.listVirtualMachines()) {
			
			if (virtualMachineDescriptor.displayName().equals(
					applicationName)) {
				descriptor = virtualMachineDescriptor;
			}
		}
		final VirtualMachine virtualMachine = attachProvider
				.attachVirtualMachine(descriptor);
		virtualMachine.loadAgent(managmentAgentJar,
				"com.sun.management.jmxremote");
		final Object portObject = virtualMachine.getAgentProperties().get(
				"com.sun.management.jmxremote.localConnectorAddress");

		final JMXServiceURL target = new JMXServiceURL(portObject + "");
		final JMXConnector connector = JMXConnectorFactory.connect(target);
		final MBeanServerConnection remote = connector
				.getMBeanServerConnection();

		final ObjectName objectName = new ObjectName(
				"Catalina:type=Manager,host=localhost,context=/");
		System.out.println("Start");

		System.out.println("ActiveSession = "
				+ remote.getAttribute(objectName, "activeSessions"));
		System.out.println("End");

	}
}
