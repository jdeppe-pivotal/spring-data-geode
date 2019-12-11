/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Utility class for resolving where class dependencies reside (within jars or directories).
 *
 * @author Jens Deppe
 */
public class ClassDependencyResolver {

  private List<String> classNames = new ArrayList<>();

  private List<String> exclusions = new ArrayList<>();

  public ClassDependencyResolver() {
  }

  public ClassDependencyResolver withClasses(Class<?>... classes) {
    this.classNames.addAll(Arrays.stream(classes).map(Class::getName).collect(Collectors.toList()));
    return this;
  }

  public ClassDependencyResolver withClasses(List<Class<?>> classes) {
    this.classNames.addAll(classes.stream().map(Class::getName).collect(Collectors.toList()));
    return this;
  }

  public ClassDependencyResolver excluding(String... exclusions) {
    this.exclusions.addAll(Arrays.stream(exclusions).collect(Collectors.toList()));
    return this;
  }

  /**
   * @param createJarFromDir if set to true will create and return a temporary jar file for each
   * discovered directory entry containing all of the entries in that directory. Each jar entry will
   * have a path relative to the original directory.
   */
  public Set<URI> process(boolean createJarFromDir) throws IOException {
    Set<URI> results = new HashSet<>();

    try (ScanResult scanResult = new ClassGraph()
        .whitelistPackages()
        .enableInterClassDependencies()
        .scan(1)) {

      Map<ClassInfo, ClassInfoList> dependencyMap = scanResult.getClassDependencyMap();
      Set<ClassInfo> seen = new HashSet<>();

      if (classNames != null) {
        for (String clazz : classNames) {
          ClassInfo rootClass = scanResult.getClassInfo(clazz);
          if (dependencyMap.containsKey(rootClass)) {
            accumulateJars(new HashSet<>(dependencyMap.get(rootClass)), dependencyMap, results, seen);
          }
        }
      } else {
        results.addAll(dependencyMap.keySet().stream()
            .map(ClassInfo::getClasspathElementURI)
            .collect(Collectors.toSet()));
      }
    }

    exclusions.forEach(e -> results.removeIf(x -> x.toString().contains(e)));
    results.removeIf(x -> !x.getScheme().equalsIgnoreCase("file"));

    if (createJarFromDir) {
      convertDirsToZips(results);
    }

    return results;
  }

  private void convertDirsToZips(Set<URI> originalJars) throws IOException {
    List<URI> dirs = originalJars.stream().filter(j -> Files.isDirectory(Paths.get(j)))
        .collect(Collectors.toList());

    if (!dirs.isEmpty()) {
      Path tempDir = Files.createTempDirectory("dependency-resolver");
      tempDir.toFile().deleteOnExit();

      for (URI oneDir : dirs) {
        URI newJar = zipDirContents(tempDir, oneDir);
        originalJars.add(newJar);
      }
    }

    originalJars.removeIf(j -> dirs.contains(j));
  }

  private URI zipDirContents(Path tempDir, URI dir) throws IOException {
    // Use the dirname to create the name of our new jar
    String newJarFilename = Paths.get(dir).getFileName().toString() + "-dir.jar";
    Path newJarPath = Paths.get(tempDir.toString(), newJarFilename);
    newJarPath.toFile().deleteOnExit();
    Path sourcePath = Paths.get(dir);

    try (ZipOutputStream zStream = new ZipOutputStream(Files.newOutputStream(newJarPath));
         Stream<Path> paths = Files.walk(sourcePath)) {
      paths
          .filter(path -> !Files.isDirectory(path))
          .forEach(path -> {
            ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
            try {
              zStream.putNextEntry(zipEntry);
              Files.copy(path, zStream);
              zStream.closeEntry();
            } catch (IOException ex) {
              throw new UncheckedIOException(ex);
            }
          });
    }

    return newJarPath.toUri();
  }

  private void accumulateJars(Set<ClassInfo> roots, Map<ClassInfo, ClassInfoList> dependencies,
                              Set<URI> accumulated, Set<ClassInfo> seen) {
    Set<ClassInfo> nextRoots = new HashSet<>();

    for (ClassInfo info : roots) {
      if (seen.contains(info)) {
        continue;
      }

      accumulated.add(info.getClasspathElementURI());
      seen.add(info);

      nextRoots.addAll(dependencies.get(info));
    }

    if (nextRoots.size() > 0) {
      accumulateJars(nextRoots, dependencies, accumulated, seen);
    }
  }

}
