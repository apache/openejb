/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util.resources;

/**
 *
 * @author <a href="mailto:adc@toolazydogs.com">Alan Cabrera</a>
 */
public class Messages_fr_FR extends org.openejb.util.resources.Messages {

 	/*
 	 * Error code prefixes:
 	 *
 	 * as   = Assembler
 	 * cc   = Container
 	 * cm   = ContainerManager
 	 * cs   = ContainerSystem
 	 * di   = DeploymentInfo
 	 * ge   = General Exception
 	 * im   = InstanceManager
 	 * ps   = PassivationStrategy
	 * sa   = Server adapter
	 * se   = Serializer
 	 * ss   = SecurityService
 	 * ts   = TransactionService
	 */

	/*
	 * I don't really know French.
	 */
 	static final Object[][] contents = {
 	// LOCALIZE THIS
 		{"ge0001", "MORTELLE ERREUR: Erreur inconnue dans le {0}.  Veuillez envoyer la trace de pile suivante et ce message à openejb-bugs@exolab.org :\n {1}"},
 		{"ge0002", "L'objet exigé de propriétés nécessaire par {0} est nul."},// param 0 is the part of the system that needs the Properties object.
 		{"ge0003", "Le {0} de fichier de propriétés pour {1} n'a pas été trouvé."}, //param 0 is the properties file name, param 1 is the part of the system that needs the properties file.
 		{"ge0004", "Entrée d'environnement {0} non trouvée dedans {1}."}, //param 0 is the property name, param 1 is the properties file name.
 		{"ge0005", "L'entrée d'environnement {0} contient une valeur illégale de {1}."}, //param 0 is the property name, param 1 is the illegal value.
 		{"ge0006", "L'entrée d'environnement {0} contient une valeur illégale de {1}. {2}"}, //param 0 is the property name, param 1 is the illegal value, param 2 is an additional message.
 		{"ge0007", "{0} ne peut pas trouver et charger la classe {1}."}, //param 0 is the part of the system that needs the class, param 1 is the class that cannot be found.
 		{"ge0008", "{0} ne peut pas l'instaniate la classe {1}, la classe ou l'initialiseur n'est pas accessible."},//param 0 is the part of the system that needs the class, param 1 is the class that cannot be accessed.
 		{"ge0009", "{0} ne peut pas l'instaniate la classe {1}, la classe peut être abstrait ou une interface."},//param 0 is the part of the system that needs the class, param 1 is the class that cannot be accessed.
 		{"as0001", "ERREUR MORTELLE: Erreur dans le fichier de configuration de XML. A reçu un {0} du programme d'analyse syntaxique énonçant le {1} à la colonne {2} de la ligne {3}. "},// param 0 type of error, param 1 error message from parser, param 2 line number, param 3 column number.
 		{"sa0001", "{0}: La connexion a été remise à l'état initial par le pair."},//param 0 is the name of the server adapter.

 	// END OF MATERIAL TO LOCALIZE
 	};

 	public Object[][] getContents() {
 		return contents;
 	}
}
