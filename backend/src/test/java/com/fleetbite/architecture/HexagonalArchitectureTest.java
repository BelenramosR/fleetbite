package com.fleetbite.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verificación completa de arquitectura hexagonal (jMolecules + ArchUnit).
 * <p>
 * Nota: en jMolecules Hexagonal, el núcleo ({@code @Application}) incluye domain + application.
 * No existe estereotipo {@code @Domain} en hexagonal; domain forma parte del application core.
 */
@AnalyzeClasses(packages = "com.fleetbite", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

	/**
	 * Estereotipos {@code @Application}, {@code @PrimaryPort}, {@code @SecondaryPort},
	 * {@code @PrimaryAdapter}, {@code @SecondaryAdapter}.
	 * <p>
	 * {@code LENIENT}: adapters pueden usar tipos de application/domain (persistencia mapea
	 * dominio; controllers usan DTOs de application).
	 */
	@ArchTest
	static final ArchRule jmoleculesHexagonal = JMoleculesArchitectureRules.ensureHexagonal(
			JMoleculesArchitectureRules.VerificationDepth.LENIENT);

	@ArchTest
	static final ArchRule domainMustNotDependOnSpring = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..",
					"jakarta.persistence..",
					"jakarta.servlet..")
			.because("El dominio debe permanecer libre de frameworks");

	@ArchTest
	static final ArchRule domainMustNotDependOnInfrastructure = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure..")
			.because("El dominio no puede depender de adapters");

	@ArchTest
	static final ArchRule domainMustNotDependOnApplication = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAPackage("..application..")
			.because("El dominio no puede depender de application");

	@ArchTest
	static final ArchRule applicationMustNotDependOnInfrastructure = noClasses()
			.that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure..")
			.because("Los casos de uso dependen de ports, no de adapters");

	@ArchTest
	static final ArchRule applicationMustNotDependOnSpring = noClasses()
			.that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.springframework..",
					"jakarta.persistence..")
			.because("Application debe ser agnóstica de Spring/JPA");

	@ArchTest
	static final ArchRule domainMustNotDependOnMapStructOrLombokApis = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
					"org.mapstruct..",
					"lombok..")
			.because("El dominio no debe acoplarse a MapStruct ni Lombok");

	@ArchTest
	static final ArchRule portsMustBeInterfaces = classes()
			.that().resideInAPackage("..application.port.in..")
			.or().resideInAPackage("..application.port.out..")
			.and().areTopLevelClasses()
			.and().haveSimpleNameNotContaining("package-info")
			.should().beInterfaces()
			.because("Los ports deben ser contratos (interfaces)");

	@ArchTest
	static final ArchRule primaryAdaptersMustNotDependOnSecondaryAdapters = noClasses()
			.that().resideInAPackage("..infrastructure.inbound..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure.outbound..")
			.because("Los adapters primarios no deben acoplarse a adapters secundarios");

	@ArchTest
	static final ArchRule springDataRepositoriesStayInOutboundPersistence = classes()
			.that().areAssignableTo(org.springframework.data.repository.Repository.class)
			.and().resideInAPackage("com.fleetbite..")
			.should().resideInAPackage("..infrastructure.outbound.persistence..")
			.because("Los repositorios Spring Data solo viven en adapters de persistencia");
}
