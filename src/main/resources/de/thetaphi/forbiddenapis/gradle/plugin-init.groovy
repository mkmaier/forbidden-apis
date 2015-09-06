/*
 * (C) Copyright Uwe Schindler (Generics Policeman) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Initializes the plugin and binds it to project lifecycle. */

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.PluginInstantiationException;

if (project.plugins.withType(JavaBasePlugin.class).isEmpty()) {
  throw new PluginInstantiationException('Forbidden-apis only works in projects using the java plugin.');
}

def tasks = project.getTasks();

// Define our tasks (one for each SourceSet):
def forbiddenTasks = project.sourceSets.collect { sourceSet ->
  tasks.create(sourceSet.getTaskName(FORBIDDEN_APIS_TASK_NAME, null), CheckForbiddenApis.class) { task ->
    task.classesDir = sourceSet.output.classesDir;
    task.classpath = sourceSet.compileClasspath;
    task.description = "Runs forbidden-apis checks on '${sourceSet.name}' classes.";
    task.dependsOn(sourceSet.output);
  }
}

// Create a task for all checks
def forbiddenTask = tasks.create(FORBIDDEN_APIS_TASK_NAME, DefaultTask.class) { task ->
  task.description = "Runs forbidden-apis checks.";
  task.group = JavaBasePlugin.VERIFICATION_GROUP;
  task.dependsOn(forbiddenTasks);
}

// Add our task as dependency to chain
tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(forbiddenTask);
