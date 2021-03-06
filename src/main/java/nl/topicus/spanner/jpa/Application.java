package nl.topicus.spanner.jpa;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;

import nl.topicus.spanner.jpa.entities.Customer;
import nl.topicus.spanner.jpa.entities.CustomerRepository;
import nl.topicus.spanner.jpa.entities.Invoice;
import nl.topicus.spanner.jpa.entities.InvoiceRepository;
import nl.topicus.spanner.jpa.entities.PhoneRepository;
import nl.topicus.spanner.jpa.service.EntityService;

@SpringBootApplication
public class Application
{
	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Autowired
	private EntityService service;

	public static void main(String[] args)
	{
		SpringApplication.run(Application.class);
	}

	@Bean
	public CommandLineRunner demo(CustomerRepository customerRepo, InvoiceRepository invoiceRepo,
			PhoneRepository phoneRepo)
	{
		return (args) -> {
			// reset repositories
			invoiceRepo.deleteAll();
			customerRepo.deleteAll();
			// save a couple of customers
			List<Long> customerIds = new ArrayList<>();
			customerIds.add(customerRepo.save(new Customer("Jack", "Bauer")).getId());
			customerIds.add(customerRepo.save(new Customer("Chloe", "O'Brian")).getId());
			customerIds.add(customerRepo.save(new Customer("Kim", "Bauer")).getId());
			customerIds.add(customerRepo.save(new Customer("David", "Palmer")).getId());
			customerIds.add(customerRepo.save(new Customer("Michelle", "Dessler")).getId());

			byte[] pdf = Files.readAllBytes(Paths.get(Application.class.getResource("pdf-sample.pdf").toURI()));
			List<Long> invoiceIds = new ArrayList<>();
			invoiceIds.add(invoiceRepo.save(
					new Invoice(customerRepo.findById(customerIds.get(0)).get(), "001", BigDecimal.valueOf(29.50), pdf))
					.getId());

			// create phones
			for (Long customerId : customerIds)
			{
				service.addPhone(customerId, 123);
			}
			// update phones
			for (Long customerId : customerIds)
			{
				service.setPhones(customerId, Arrays.asList(1, 2, 3));
			}

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (Customer customer : customerRepo.findAll())
			{
				log.info(customer.toString());
				service.printPhones(customer.getId());
			}
			log.info("");

			// fetch an individual customer by ID
			Customer customer = customerRepo.findById(customerIds.get(0)).get();
			log.info("Customer found with findOne(" + customerIds.get(0) + "):");
			log.info("--------------------------------");
			log.info(customer.toString());
			log.info("");

			// fetch customers by last name
			log.info("Customer found with findByLastName('Bauer'):");
			log.info("--------------------------------------------");
			for (Customer bauer : customerRepo.findByLastName("Bauer"))
			{
				log.info(bauer.toString());
			}
			log.info("");

			// fetch customers using custom query
			log.info("Customer found with custom query:");
			log.info("--------------------------------------------");
			for (Customer c : customerRepo.findCustomer(PageRequest.of(2, 100)))
			{
				log.info(c.toString());
			}
			log.info("");

			// fetch invoices
			Invoice invoice = invoiceRepo.findById(invoiceIds.get(0)).get();
			log.info("Invoice found with findOne(" + invoiceIds.get(0) + "):");
			log.info("--------------------------------");
			log.info(invoice.toString());
			log.info("");
		};
	}
}
