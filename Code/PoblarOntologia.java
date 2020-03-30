package agipront.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.XSD;

public class PoblarOntologia {
	OntModel ontologia;
	String URLOntologia = "http://www.semanticweb.org/utn/AgileOntology";
	String prefixmiont = URLOntologia + "#";
	String agiPrOntFileName= "./bin/AgiPrOnt-V2.owl";
	String fileName="./bin/Recommender - DATASETInstanciable.csv";
	String objectPropertyNames[];
	String domainNames[];
	String characteristicDataType[];
	String rangeName[];
	String characteristicName[];
	String adoptedPractice[];
	String practice[];
	
	public PoblarOntologia() {
		ontologia= this.loadOntology(agiPrOntFileName);
	}

	public OntModel loadOntology(String ontoName) {
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.read(ontoName);
		System.out.println("Se cargó la ontologia desde el archivo "+ontoName);
		return m;
	}
	
	public void saveModel() {
		Date d= new Date();
		FileOutputStream ontologyFile;
		try {
			ontologyFile = new FileOutputStream(agiPrOntFileName + d.toString()+".owl");
			ontologia.write(ontologyFile);
			System.out.println("Ontología guardada en archivo "+ agiPrOntFileName + d.toString()+".owl");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadDatasetFromCSV(){
		BufferedReader br= null;
		String line="";
		String cvsSplitBy=",";
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try{
			br = new BufferedReader(new FileReader(fileName));
			int i=1;
			while((line= br.readLine()) != null) {
				String[] description = line.split(cvsSplitBy);
				System.out.println("Linea "+i+" longitud: "+description.length);
				for(int j=0; j< description.length; j++){
					System.out.print(description[j]+", ");
				}
				System.out.println("");
				if( i==1 ) {
					// Primer linea, nombre de object properties			
					this.objectPropertyNames= description;
				} else if( i==2) {
					// domain name (caracteristica de)
					this.domainNames= description;
				} else if (i==3) {
					this.characteristicDataType= description;
				} else if (i==4) {
					// omitir
				} else if (i==5) {
					this.characteristicName= description;
				} else if (i==6) {
					this.rangeName= description;
					System.out.println("j inicializado en "+(objectPropertyNames.length+9-1)+" descrition.length "+description.length+ " elementos en el array "+(description.length-objectPropertyNames.length-9+1));
					adoptedPractice= new String[description.length-objectPropertyNames.length-9+1];
					for(int j=objectPropertyNames.length+9-1; j<description.length; j++) {
						adoptedPractice[j-(objectPropertyNames.length+9-1)]= description[j];
					}
				} else {
					// instanciar profile
					Individual ap;
					ap=this.instatiateProfile(description);
					if( description.length > (objectPropertyNames.length+9-1)) {
						practice= new String[description.length-objectPropertyNames.length-9+1];
						for(int j=objectPropertyNames.length+9-1; j<description.length; j++) {
							practice[j-(objectPropertyNames.length+9-1)]= description[j];
						}
						this.instantiatePractice(ap, adoptedPractice, practice);
					}
				}
				i++;
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally {
			if(br!= null) {
				try {
					br.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Individual instatiateProfile(String[] description) {
		// - Crear instancia AgileProfile. 
		// Se puede utilizar el nombre (col A) como rdfs:label, y 
		// el ID (col B) como valor del dataproperty id. 
		// Instancia creada ap.
		String uriName = replaceName(description[0]);
		OntClass ontoClass= ontologia.getOntClass(prefixmiont + "AgileProfile");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
				
		//int nInstProfile= countInstances(ontoClass); //onto.listIndividuals(ontoClass).toList().size();
		int nInsts= ontologia.listIndividuals(ontoClass).toList().size();
		System.out.println("OntClass "+ontoClass.getLocalName()+ " posee "+nInsts+" instancias");
		String instName= "AgileProfile_"+ (nInsts+1);
		String profileName= instName;
		//Individual ap= ontoClass.createIndividual(prefixmiont + instName);
		Individual ap= ontoClass.createIndividual(prefixmiont + uriName);
		//ap.setLabel(instName, "EN");
		//ap.setLabel(uriName, "EN");
		ap.setLabel(description[0], "EN");
		System.out.println("se creo "+ap.getURI()+ " label "+ap.getLabel("EN"));
		DatatypeProperty idDataTypeProp= ontologia.getDatatypeProperty(prefixmiont + "id");
		//instProfile.setPropertyValue(idDataTypeProp, description[1]);
		ap.addProperty(idDataTypeProp, description[1]);
		System.out.println("set property "+ idDataTypeProp.toString()+" to "+description[1]);
		// - Crear instancia AgileTeam. 
		// Esta información no está en el dataset, 
		// solo lo estamos usando para vincular un AgileProfile con un AgileTeam. 
		// Generar un nombre automáticamente usando el rdfs:label de la instancia de AgileProfile + 
		// “Team” + un número único. 
		// Instancia creada t.
		ontoClass= ontologia.getOntClass(prefixmiont + "AgileTeam");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
		nInsts= ontologia.listIndividuals(ontoClass).toList().size();
		System.out.println("OntClass "+ontoClass.getLocalName()+ " posee "+nInsts+" instancias");
		instName= profileName+ "Team"+ (nInsts+1);
		Individual t= ontoClass.createIndividual(prefixmiont + instName);
		t.setLabel(instName, "EN");
		System.out.println("se creo "+t.getURI()+ " label "+t.getLabel("EN"));
		
		// - Asignar la object property hasTeam entre ap y t.
		ObjectProperty hasTeamProp= ontologia.getObjectProperty(prefixmiont +"hasTeam");
		ap.addProperty(hasTeamProp, t);
		System.out.println("Property: "+ap.getLocalName()+" "+ hasTeamProp.getLocalName()+" "+t.getLocalName());

		// - Crear instancia AgileProject. 
		// Esta información no está en el dataset, 
		// solo lo estamos usando para vincular un AgileProfile con un AgileProject. 
		// Generar un nombre automáticamente usando el rdfs:label de la instancia de AgileProfile + 
		// “Project” + un número único. 
		// Instancia creada p
		ontoClass= ontologia.getOntClass(prefixmiont + "AgileProject");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
		nInsts= ontologia.listIndividuals(ontoClass).toList().size();
		System.out.println("OntClass "+ontoClass.getLocalName()+ " posee "+nInsts+" instancias");
		instName= profileName+ "Project"+ (nInsts+1);
		Individual p= ontoClass.createIndividual(prefixmiont + instName);
		p.setLabel(instName, "EN");
		System.out.println("se creo "+p.getURI()+ " label "+p.getLabel("EN"));

		// - Asignar la object property hasProject entre ap y p.
		ObjectProperty hasProjectProp= ontologia.getObjectProperty(prefixmiont +"hasProject");
		ap.addProperty(hasProjectProp, p);
		System.out.println("Property: "+ap.getLocalName()+" "+ hasProjectProp.getLocalName()+" "+p.getLocalName());

		// B) INSTANCIACIÓN DE CARACTERÍSTICAS DE CADA PERFIL AGIL
		Individual ch, value;
		String chName;
		String propName;
		String characName;
		String valor;
		ObjectProperty hasProp;
		Boolean isBinary;
		int cont= 1;
		//System.out.println("length "+this.objectPropertyNames.length);
		while((cont+1) < this.objectPropertyNames.length) {
			// las primeras dos columnas se omiten
			chName= this.rangeName[cont+1];
			propName= this.objectPropertyNames[cont +1];
			characName= this.characteristicName[cont + 1].concat(this.characteristicDataType[cont + 1]);
			isBinary= this.characteristicDataType[cont+1].equals("BinaryValue");
			if( description.length > (cont+1) ) {
				valor= description[cont+1];
				System.out.println((cont+1) +"Instanciar: "+profileName+" "+ chName+" "+ propName+" "+ characName+" "+ valor+" "+isBinary);
				instantiateCharacteristic(profileName, chName, propName, characName, valor, ap, t, p, isBinary);
			}			
			cont++;
		}
		return ap;
	}
	
	public void instantiateCharacteristic(String profileName, String chName, String propName, String characName, String valor, Individual ap, Individual t, Individual p, Boolean isBinary) {
		Individual ch, value;
		ObjectProperty hasProp;
		OntClass ontoClass;
		int nInsts;
		String instName;
		
		if (!valor.equals("")) {
			// - generar una instancia de cada clase de caracteristica (ch). 
			// (Se pueden ir recorriendo por clases), realizado manualmente.
			ontoClass= ontologia.getOntClass(prefixmiont + chName);
			System.out.println("Se obtuvo "+ontoClass);
			nInsts= ontologia.listIndividuals(ontoClass).toList().size();
			instName= profileName+ chName + (nInsts+1);
			ch= ontoClass.createIndividual(prefixmiont + instName);
			ch.setLabel(instName, "EN");

			// Vincular a ap mediante la object property correspondiente 
			// (es alguna de las subclases de hasCharacteristic) con ch
			hasProp= ontologia.getObjectProperty(prefixmiont + propName);
			if(propName.equals("hasTeamCharacteristic"))
				t.addProperty(hasProp, ch);
			else if(propName.equals("hasProjectCharacteristic"))
				p.addProperty(hasProp, ch);
			else
				ap.addProperty(hasProp, ch);

			// Obtener del dataset el valor de la característica que corresponde al perfil 
			// (en el dataset es un número, 
			// hay que tener un mecanismo para vincularlo con la instancia de valor correspondiente. 
			// Las instancias de valores ya estarán creadas para cada clase de valor de característica 
			// posible (instancias singleton):
			//int val= Integer.parseInt(valor);
			if(!isBinary) {
				OntClass valClass= ontologia.getOntClass(prefixmiont + characName);
				System.out.println("Se recupero la clase "+valClass.toString());
			
				ExtendedIterator<Individual> itInds= ontologia.listIndividuals(valClass);
				value=null;
				while (itInds.hasNext()) {
					Individual i= itInds.next();
					DatatypeProperty dt= ontologia.getDatatypeProperty(prefixmiont + "value");
					System.out.print(i);
					System.out.print(" "+dt.toString());				
					System.out.println(" "+i.getPropertyValue(dt));
					String v=i.getPropertyValue(dt).asLiteral().toString();
					System.out.println(v);
					if(v.equals(valor)) {
						value= i;
						break;
					}
				}
			
				// Faltaría vincular la ch con el valor "hasValue"
				hasProp= ontologia.getObjectProperty(prefixmiont + "hasValue");
				ch.addProperty(hasProp, value);
			} else {
				OntClass valClass= ontologia.getOntClass(prefixmiont + "BinaryValue");
				System.out.println("Se recupero la clase "+valClass.toString());
			
				ExtendedIterator<Individual> itInds= ontologia.listIndividuals(valClass);
				value=null;
				while (itInds.hasNext()) {
					Individual i= itInds.next();
					DatatypeProperty dt= ontologia.getDatatypeProperty(prefixmiont + "value");
					System.out.print(i);
					System.out.print(" "+dt.toString());				
					System.out.println(" "+i.getPropertyValue(dt));
					String v=i.getPropertyValue(dt).asLiteral().toString();
					System.out.println(v);
					if(v.equals(valor)) {
						value= i;
						break;
					}
				}
			
				// Faltaría vincular la ch con el valor "hasValue"
				hasProp= ontologia.getObjectProperty(prefixmiont + "hasValue");
				ch.addProperty(hasProp, value);
				
			}
		}

	}

	public void instantiatePractice(Individual ap, String[] adoptedPractice, String[] practice) {
		// Ver si el perfil tiene un valor en la celda para la practica, 
		// significa que esa práctica fue usada por el perfil agil. 
		// Para cada practica pr
		for(int i=0; i< adoptedPractice.length; i++) {
			String practicesSplitBy="/";
			String[] names = adoptedPractice[i].split(practicesSplitBy);
			System.out.println("Procesando "+ adoptedPractice[i]+ " names length "+names.length);		
			String value= practice[i];
			if(!value.equals("")) {
				// Pueden venir varios nombres separados por /
				for(int k=0; k< names.length;k++) {
					String prName= names[k];
					
					System.out.println("Procesando practica "+prName+ " con valor "+value);
					// Recuperar la instancia singleton correspondiente a la practica (instancia pr). 
					// No hay que crearla.
					Individual pr= ontologia.getIndividual(prefixmiont + prName);
					if(pr!= null) {
						System.out.println("Se recuperó el individuo pr "+pr.getLocalName());
						// Crear una instancia de AdoptsPracticeRelation (apr)
						OntClass ontoClass= ontologia.getOntClass(prefixmiont + "AdoptsPracticeRelation");
						int nInsts= ontologia.listIndividuals(ontoClass).toList().size();
						String instName= "AdoptsPracticeRelation_"+ (nInsts+1);
						Individual apr= ontoClass.createIndividual(prefixmiont + instName);
						System.out.println("Se creo el individuo apr "+apr.getLocalName());
						// Asociar a ap con la instancia apr mediante la objectProperty profileAdoptsPractice
						ObjectProperty hasProp= ontologia.getObjectProperty(prefixmiont +"profileAdoptsPractice");
						System.out.println("Se obtuvo la property "+hasProp.getLocalName());
						ap.addProperty(hasProp, apr);
						// Asociar a apr con valor dado por la instancia pr mediante 
						// la objectProperty adoptedPractice 
						hasProp= ontologia.getObjectProperty(prefixmiont +"adoptedPractice");
						System.out.println("Se obtuvo la property "+hasProp.getLocalName());
						apr.addProperty(hasProp, pr);
						// Asociar la instancia apr con la instancia lickert que representa al valor de éxito 
						// que estará dado por una instancia de la clase de LickertScale 
						// (el valor se toma del data set) mediante la objectProperty lickertScaleValue.
						hasProp= ontologia.getObjectProperty(prefixmiont +"likertScaleValue");
						System.out.println("Se obtuvo la property "+hasProp.getLocalName()+ " se va a asociar con el valor "+value);
						Individual lickert = null;
						if(value.equals("1")) {
							lickert= ontologia.getIndividual(prefixmiont + "LickerScaleValue_1");
						} else if(value.equals("2")) {
							lickert= ontologia.getIndividual(prefixmiont + "LickertScaleValue_2");
						} else if(value.equals("3")) {
							lickert= ontologia.getIndividual(prefixmiont + "LickertScaleValue_3");
						} else if(value.equals("4")) {
							lickert= ontologia.getIndividual(prefixmiont + "LickertScaleValue_4");
						} else if(value.equals("5")) {
							lickert= ontologia.getIndividual(prefixmiont + "LickertScaleValue_5");
						} else {
							System.out.println("Lickert scale error "+value);
						}
						apr.addProperty(hasProp, lickert);
					} else
						System.out.println("Error, "+prName+" no encontrada");
				}
			}
			


		}		
	}

	
	public void extenderOntology() {
		OntClass ontoClass= ontologia.getOntClass(prefixmiont + "CharacteristicValue");
		DatatypeProperty dt= ontologia.createDatatypeProperty(prefixmiont + "value");
		dt.setDomain(ontoClass);
		dt.setRange(XSD.integer);
		
		Individual i= ontologia.getIndividual(prefixmiont + "False");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("0"));
		
		i= ontologia.getIndividual(prefixmiont + "True");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		
		i= ontologia.getIndividual(prefixmiont + "3OrLess(toosmall)");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "4To6");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "7(optimal)");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "8To9");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "10+(toobig)");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "siloedTeams");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "mostMembersAreSpecialists");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "50/50");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "mostMembersAreMultipleSkilled");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "fullCrossfunctional");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "neutral");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "lowImpact");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "moderatedImpact");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "moderated-to-hight");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "highImpact");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "sameLocation");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "sameCountry");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "international");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "differentContinents");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "global(differentTimezones)");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "LowRate");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "mediumToLow");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "mediumRate");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "mediumToHigh");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "HighRate");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "never");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "1Project");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "2-3Projects");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "3-4Projects");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "5+");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "lowExperience");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "low-to-acceptable");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "acceptableExperience");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "acceptable-to-high");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "highExperience");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));

		i= ontologia.getIndividual(prefixmiont + "low");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "moderated");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "medium");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "medium-high");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "high");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		i= ontologia.getIndividual(prefixmiont + "critical");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		i= ontologia.getIndividual(prefixmiont + "low-medium");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "highSecurity");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		
		i= ontologia.getIndividual(prefixmiont + "noInnovation");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "lowInnovation");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "moderatedInnovation");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "highInnovation");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "advanced");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "OnshoreOutsourcing");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "OffshoreOutsourcing");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "OnshoreInsourcing");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "OffshoreInsourcing");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "Nearshore");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "lessThan$1Million");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "$1-2Millions");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "$2-3Millions");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "$3-4Millions");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "$5Millions");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "lessThan3Months");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "3-6Months");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "6-9Months");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "9-12Months");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "morethan12Months");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "noBackground");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "bad");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "adequate");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "veryGood");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "expert");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "nobackground/untrained");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "littleTrained");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "acceptable");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "wellTrained");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));

		i= ontologia.getIndividual(prefixmiont + "inflexible");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "notVeryFlexible");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "flexible");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "veryFlexible");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
		
		i= ontologia.getIndividual(prefixmiont + "vague/changeFrequently");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("1"));
		i= ontologia.getIndividual(prefixmiont + "frequent-to-moderated");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("2"));
		i= ontologia.getIndividual(prefixmiont + "moderatedStability");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("3"));
		i= ontologia.getIndividual(prefixmiont + "infrequent");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("4"));
		i= ontologia.getIndividual(prefixmiont + "defined/stable");
		System.out.println("Se obtuvo el individual "+ i.toString());
		i.setPropertyValue(dt, ontologia.createTypedLiteral("5"));
}
	
	public void limpiarOntology() {
		OntClass ontoClass= ontologia.getOntClass(prefixmiont + "AgileProfile");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
		
		// Limpiar instancias de ontoClass
		Individual i1= ontologia.getIndividual(prefixmiont + "Prueba1");
		System.out.println("Se recuperó el individuo "+ i1.toString());
		i1.remove();
		
		i1= ontologia.getIndividual(prefixmiont + "Prueba2");
		System.out.println("Se recuperó el individuo "+ i1.toString());
		i1.remove();
		
		ontoClass= ontologia.getOntClass(prefixmiont + "AgileTeam");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
		printInstances(ontoClass);
		i1= ontologia.getIndividual(prefixmiont + "ScrumTeam1");
		System.out.println("Se recuperó el individuo "+ i1.toString());
		i1.remove();
		
		ontoClass= ontologia.getOntClass(prefixmiont + "AgileProject");
		System.out.println("Se recuperó la clase "+ ontoClass.toString());
		printInstances(ontoClass);
		i1= ontologia.getIndividual(prefixmiont + "Project1");
		System.out.println("Se recuperó el individuo "+ i1.toString());
		i1.remove();

		i1= ontologia.getIndividual(prefixmiont + "TeamSize1");
		System.out.println("Se recuperó el individuo "+ i1.toString());
		i1.remove();
		
	}
	
	private String replaceName(String name){
		String result= name.replace(' ', '_').replace(',', '-');
		result= result.replace("(", "-_").replace(")", "_-");
		return result;
	}


	public void printInstances(OntClass ontoClass){
		ExtendedIterator<Individual> itInds= ontologia.listIndividuals(ontoClass);
		while (itInds.hasNext()) {
			System.out.println(itInds.next());
		}

	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PoblarOntologia poblar= new PoblarOntologia();
		poblar.extenderOntology();
		poblar.limpiarOntology();
		poblar.loadDatasetFromCSV();
		poblar.saveModel();
	}

}
