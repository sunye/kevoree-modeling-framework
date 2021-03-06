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


package org.kevoree.tools.ecore.gencodeKotlin.loader

import java.io.{File, FileOutputStream, PrintWriter}
import org.eclipse.emf.ecore.EClass
import org.kevoree.tools.ecore.gencodeKotlin.{GenerationContext, ProcessorHelper}

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 25/09/11
 * Time: 15:25
 */

class ContextGenerator(ctx:GenerationContext, genDir: String, genPackage: String, elementType: EClass, modelPackage : String) {


  def generateContext() {
     val pr = new PrintWriter(new File(genDir + "/" + elementType.getName + "LoadContext.kt"),"utf-8")

    pr.println("package " + genPackage + ";")
    pr.println()
    pr.println("import xml.NodeSeq")
    pr.println("import " + modelPackage + "._")
    pr.println()

    pr.println("class " + elementType.getName + "LoadContext {")

    pr.println()
    pr.println("\t\tvar xmiContent : NodeSeq = null")
    pr.println()
    pr.println("\t\tvar "+elementType.getName.substring(0,1).toLowerCase + elementType.getName.substring(1)+" : "+ ProcessorHelper.fqn(ctx,elementType)+" = null")
    pr.println()
    pr.println("\t\tval map : scala.collection.mutable.Map[String, Any] = new scala.collection.mutable.HashMap[String, Any]()")
    pr.println()
    pr.println("\t\tval stats : scala.collection.mutable.Map[String, Int] = new scala.collection.mutable.HashMap[String, Int]()")
    pr.println()

    pr.println("}")

    pr.flush()
    pr.close()
  }

}