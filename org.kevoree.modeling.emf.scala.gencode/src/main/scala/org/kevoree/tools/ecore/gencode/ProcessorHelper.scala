/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors:
 * 	Fouquet Francois
 * 	Nain Gregory
 */


package org.kevoree.tools.ecore.gencode

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.JavaConversions._
import collection.mutable.Buffer
import org.eclipse.emf.ecore._

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 22/09/11
 * Time: 09:49
 */

object ProcessorHelper {

  def checkOrCreateFolder(path: String) {
    val file = new File(path)
    if (!file.exists) file.mkdirs
  }


  def convertType(aType: EDataType): String = {
    aType match {
      case theType:EEnum => theType.getName
      case _@theType => convertType(theType.getInstanceClassName)
      //case _ => throw new UnsupportedOperationException("ProcessorHelper::convertType::No matching found for type: " + aType.getClass); null
    }
  }

  def convertType(theType: String): String = {
    theType match {
      case "bool" | "boolean" | "java.lang.Boolean" => "java.lang.Boolean"
      case "java.lang.String" | "String" => "java.lang.String"
      case "int" | "java.lang.Integer" => "java.lang.Integer"
      case "float" | "java.lang.Float" => "java.lang.Float"
      case "double" | "java.lang.Double" => "java.lang.Double"
      case "long" | "java.lang.Long" => "java.lang.Long"
      case "java.lang.Object" => "java.lang.Object"
      case "java.util.Date" => "java.util.Date"
      case _ => throw new UnsupportedOperationException("ProcessorHelper::convertType::No matching found for type: " + theType); null
    }
  }

  def protectReservedWords(word: String): String = {
    word match {
      case "type" => "`type`"
      case "object" => "`object`"
      case "requires" => "`requires`"
      case _ => word //throw new UnsupportedOperationException("ProcessorHelper::protectReservedWords::No matching found for word: " + word);null
    }
  }

  def generateHeader(packElement: EPackage): String = {
    var header = "";
    val formateur = new SimpleDateFormat("'Date:' dd MMM yy 'Time:' HH:mm")
    header += "/**\n"
    header += " * Created by Ecore Model Generator.\n"
    header += " * @authors: Gregory NAIN, Fouquet Francois\n"
    header += " * " + formateur.format(new Date) + "\n"
    header += " * Meta-Model:NS_URI=" + packElement.getNsURI + "\n"
    header += " */"
    header
  }


  def generateSuperTypes(ctx : GenerationContext, cls: EClass, packElement: EPackage): Option[String] = {
    var superTypeList: Option[String] = None
    superTypeList = Some(" extends " + ctx.getKevoreeContainer.get)
    // superTypeList = Some(superTypeList.get + "with " + packElement.getName.substring(0, 1).toUpperCase + packElement.getName.substring(1) + "Mutable")
    cls.getESuperTypes.foreach {
      superType => superTypeList = Some(superTypeList.get + " with " + ProcessorHelper.fqn(ctx,superType))
    }
    superTypeList
  }

  def getAllConcreteSubTypes(iface : EClass) : List[EClass] = {
    var res = List[EClass]()
    iface.getEPackage.getEClassifiers.filter(cl => cl.isInstanceOf[EClass]).foreach{cls=>
      if(!cls.asInstanceOf[EClass].isInterface
        && !cls.asInstanceOf[EClass].isAbstract
        && cls.asInstanceOf[EClass].getEAllSuperTypes.contains(iface)) {

        if(!res.exists(previousC => cls.asInstanceOf[EClass].getEAllSuperTypes.contains(previousC))){
          res = List(cls.asInstanceOf[EClass]) ++ res
        } else {
          res = res ++ List(cls.asInstanceOf[EClass])
        }
      }
    }
    res
  }
  def getDirectConcreteSubTypes(iface : EClass) : List[EClass] = {
    var res = List[EClass]()
    iface.getEPackage.getEClassifiers.filter(cl => cl.isInstanceOf[EClass]).foreach{cls=>
      if(!cls.asInstanceOf[EClass].isInterface
        && !cls.asInstanceOf[EClass].isAbstract
        && cls.asInstanceOf[EClass].getEAllSuperTypes.contains(iface)) {

        //adds an element only if the collection does not already contain one of its supertypes
        if(!res.exists(previousC => cls.asInstanceOf[EClass].getEAllSuperTypes.contains(previousC))){
          //remove potential subtypes already inserted in the collection
          res = res.filterNot{ c => c.asInstanceOf[EClass].getEAllSuperTypes.contains(cls)}
          res = res ++ List(cls.asInstanceOf[EClass])
        }
      }
    }
    res
  }


  def getPackageGenDir(ctx:GenerationContext, pack:EPackage) : String = {
    var modelGenBaseDir = ctx.getRootGenerationDirectory.getAbsolutePath + "/"
    ctx.getPackagePrefix.map(prefix => modelGenBaseDir += prefix.replace(".", "/") + "/")
    modelGenBaseDir += fqn(pack).replace(".", "/") + "/"
    modelGenBaseDir
  }

  /**
   * Computes the Fully Qualified Name of the package in the context of the model.
   * @param pack the package which FQN has to be computed
   * @return the Fully Qualified package name
   */
  def fqn(pack:EPackage) : String = {
    var locFqn = protectReservedWords(pack.getName)
    var parentPackage = pack.getESuperPackage;
    while(parentPackage != null) {
      locFqn = parentPackage.getName + "." + locFqn
      parentPackage = parentPackage.getESuperPackage
    }
    locFqn
  }

  /**
   * Computes the Fully Qualified Name of the package in the context of the generation (i.e. including the package prefix if any).
   * @param ctx the generation context
   * @param pack the package which FQN has to be computed
   * @return the Fully Qualified package name
   */
  def fqn(ctx:GenerationContext, pack:EPackage) : String = {
    ctx.getPackagePrefix match {
      case Some(prefix) => {
        if(prefix.endsWith(".")) {
          prefix + fqn(pack)
        } else {
          prefix + "." + fqn(pack)
        }
      }
      case None => fqn(pack)
    }
  }

  /**
   * Computes the Fully Qualified Name of the class in the context of the model.
   * @param cls the class which FQN has to be computed
   * @return the Fully Qualified Class name
   */
  def fqn(cls:EClassifier) : String = {
    fqn(cls.getEPackage) + "." + cls.getName
  }

  /**
   * Computes the Fully Qualified Name of the class in the context of the generation (i.e. including the package prefix if any).
   * @param ctx the generation context
   * @param cls the class which FQN has to be computed
   * @return the Fully Qualified Class name
   */
  def fqn(ctx:GenerationContext, cls:EClassifier) : String = {
    fqn(ctx, cls.getEPackage) + "." + cls.getName
  }






  private def lookForRootElementByName(rootXmiPackage : EPackage, rootContainerClassName: String) : Option[EClass] = {
    var possibleRoot : Option[EClass] = None
    //Search in the current package
    if(rootXmiPackage.getEClassifiers.forall(classifier=>{
      classifier match {
        case cls : EClass => {
          if(cls.getName.equals(rootContainerClassName)) {
            possibleRoot = Some(cls)
            false
          }else {
            true
          }
        }
        case _ => true
      }
    })) { // not found, looking into sub-packages
      rootXmiPackage.getESubpackages.forall(subpackage =>{
        possibleRoot = lookForRootElementByName(subpackage, rootContainerClassName)
        possibleRoot match {
          case Some(cls) => false
          case None => true
        }
      })
    }
    possibleRoot
  }

  def lookForRootElement(rootXmiPackage : EPackage, rootContainerClassName: Option[String] = None) : Option[EClass] = {

    var referencedElements: List[String] = List[String]()
    var possibleRoot : Option[EClass] = None

    rootContainerClassName match {
      case Some(className) => {
        possibleRoot = lookForRootElementByName(rootXmiPackage, className)
      }
      case None =>{
        rootXmiPackage.getEClassifiers.foreach {
          classifier => classifier match {
            case cls: EClass => {
              //System.out.println("Class::" + cls.getName)
              cls.getEAllContainments.foreach {
                reference =>
                  if (!referencedElements.contains(reference.getEReferenceType.getName)) {
                    referencedElements = referencedElements ++ List(reference.getEReferenceType.getName)
                    reference.getEReferenceType.getEAllSuperTypes.foreach{st =>
                      if (!referencedElements.contains(st.getName)) {
                        referencedElements = referencedElements ++ List(st.getName)
                      }
                    }
                  }
                //System.out.println("\t\tReference::[name:" + reference.getName + ", type:" + reference.getEReferenceType.getName + ", isContainement:" + reference.isContainment + ", isContainer:" + reference.isContainer + "]")
              }
            }
            case _@e => throw new UnsupportedOperationException(e.getClass.getName + " did not match anything while looking for containerRoot element.")
          }
        }

        //System.out.println("References:" + referencedElements.mkString(","))

        rootXmiPackage.getEClassifiers.filter {
          classif =>
            classif match {
              case cls: EClass => {

                if(cls.getEAllContainments.size() == 0) {
                  false
                } else if (referencedElements.contains(cls.getName)) {
                  false
                } else {
                  // System.out.println(cls.getEAllSuperTypes.mkString(cls.getName +" supertypes [\n\t\t",",\n\t\t","]"))
                  cls.getEAllSuperTypes.find{st =>
                    val mat = referencedElements.contains(st.getName)
                    // System.out.println(st.getName + " Match:: " + mat)
                    mat
                  } match {
                    case Some(s) => false
                    case None => true
                  }
                }
              }
              case _ => false
            }
        } match {
          case b: Buffer[EClassifier] => {
            if (b.size != 1) {
              b.foreach(classifier => System.out.println("Possible containerRoot:" + classifier.getName))
            } else {
              System.out.println("RootElement:" + b.get(0).getName)
              possibleRoot = Some(b.get(0).asInstanceOf[EClass])
            }
          }
          case _@e => System.out.println("Root element not found. Returned:" + e.getClass);null
        }
      }
    }
    possibleRoot
  }

}