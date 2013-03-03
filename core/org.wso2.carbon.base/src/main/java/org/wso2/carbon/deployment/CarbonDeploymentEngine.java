/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.api.CarbonDeploymentService;
import org.wso2.carbon.deployment.scheduler.CarbonDeploymentIterator;
import org.wso2.carbon.deployment.scheduler.CarbonScheduler;
import org.wso2.carbon.deployment.scheduler.CarbonSchedulerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Deployment Engine of Carbon which manages the deployment/unDeployment of artifacts in carbon.
 */

public class CarbonDeploymentEngine implements CarbonDeploymentService {

    private static final Log log = LogFactory.getLog(CarbonDeploymentEngine.class);

    private CarbonRepositoryListener repositoryListener;

    private File repositoryDirectory = null;

    private Map<String, Map<String, Deployer>> deployerMap =
            new HashMap<String, Map<String, Deployer>>();

    private Lock lock = new ReentrantLock();

    private CarbonScheduler carbonScheduler;

    private CarbonSchedulerTask carbonSchedulerTask;

    private List artifactsToDeploy = new ArrayList();

    private List artifactsToUnDeploy = new ArrayList();

    private Map deployedArtifacts = new HashMap();

    private List artifactsList = new ArrayList();


    public CarbonDeploymentEngine(String repositoryDir) throws CarbonDeploymentException {
        init(repositoryDir);
    }

    /**
     * Configure and prepare the repository associated with this engine.
     *
     * @throws CarbonDeploymentException on error
     */
    public void init(String repositoryDir) throws CarbonDeploymentException {
        repositoryDirectory = new File(repositoryDir);
        if (!repositoryDirectory.exists()) {
            throw new CarbonDeploymentException("Cannot find repository : " + repositoryDirectory);
        }
        repositoryListener = new CarbonRepositoryListener(this);
    }

    /**
     * Add and initialize a new Deployer.
     *
     * @param deployer  the deployer
     * @param directory the directory which will be scanned for deployable artifacts
     * @param extension the extension of the deployable artifacts for this Deployer
     */
    public void addDeployer(Deployer deployer, String directory, String extension) {

        if (deployer == null) {
            log.error("Failed to add Deployer : Deployer Class Name is null");
            return;
        }

        if (directory == null) {
            log.error("Failed to add Deployer " + deployer.getClass().getName() +
                      ": missing 'directory' attribute");
            return;
        }

        lock.lock();
        try {
            Map<String, Deployer> extensionMap = deployerMap.get(directory);
            if (extensionMap == null) {
                extensionMap = new HashMap<String, Deployer>();
                deployerMap.put(directory, extensionMap);
            }
            extensionMap.put(extension, deployer);

        } finally {
            lock.unlock();
        }
    }

    /**
     * @return
     */
    public Map<String, Map<String, Deployer>> getDeployers() {
        return this.deployerMap;
    }

    /**
     *
     *
     */
    public void loadArtifacts() {
        repositoryListener.start();
        startSchedulerTask();
    }


    /**
     * Starts the Deployment engine to perform Hot deployment and so on.
     */
    public void startSchedulerTask() {
        carbonScheduler = new CarbonScheduler();
        carbonSchedulerTask = new CarbonSchedulerTask(repositoryListener);
        carbonScheduler.schedule(carbonSchedulerTask, new CarbonDeploymentIterator());
    }


    public File getRepositoryDirectory() {
        return repositoryDirectory;
    }

    /**
     * Retrieve the deployer from the current deployers set, by giving the associated
     * extension and direction
     *
     * @param directory associated directory of the deployer
     * @param extension associated extension of the deployer
     * @return Deployer instance
     */
    public Deployer getDeployer(String directory, String extension) {
        Map<String, Deployer> extensionMap = deployerMap.get(directory);
        return (extensionMap != null) ? extensionMap.get(extension) : null;
    }


    /**
     * Add given artifact to the list artifacts to deploy
     *
     * @param artifact deployment file data to deploy
     */
    public synchronized void addArtifactToDeploy(
            Artifact artifact) {
        Artifact deployedArtifact = (Artifact) deployedArtifacts.get(artifact.getAbsolutePath());
        if (deployedArtifact != null) {
            if (CarbonDeploymentUtils.isArtifactModified(deployedArtifact.getFile(), deployedArtifact)) {
                artifactsToUnDeploy.add(deployedArtifact);
                artifactsToDeploy.add(deployedArtifact);
            }
        } else {
            artifactsToDeploy.add(artifact);
            CarbonDeploymentUtils.setArtifactLastModifiedTime(artifact.getFile(), artifact);
        }
        artifactsList.add(artifact.getAbsolutePath());
    }

    /**
     * Add given artifact to the list fo artifact to be unDeployed
     *
     * @param artifact deployment file data to unDeploy
     */
    public synchronized void addArtifactToUnDeploy(
            Artifact artifact) {
        artifactsToUnDeploy.add(artifact);
    }

    /**
     * Deploy the artifacts found in the artifacts to be deployed list
     */
    public void deployArtifacts() {
        try {
            if (artifactsToDeploy.size() > 0) {
                for (Object artifact : artifactsToDeploy) {
                    Artifact artifactToDeploy = (Artifact) artifact;
                    try {
                        String directory = artifactToDeploy.getType();
                        Deployer deployer = getDeployer(directory,
                                                        artifactToDeploy.getFileExtension());
                        deployer.deploy(artifactToDeploy);
                        deployedArtifacts.put(artifactToDeploy.getAbsolutePath(), artifact);
                    } catch (CarbonDeploymentException e) {
                        log.error(e);
                    }
                }
            }
        } finally {
            artifactsToDeploy.clear();
        }
    }

    /**
     * UnDeploy the artifacts found in the artifact to be unDeployed list
     */
    public void unDeployArtifacts() {
        try {
            if (artifactsToUnDeploy.size() > 0) {
                for (Object artifact : artifactsToUnDeploy) {
                    Artifact artifactToUnDeploy = (Artifact) artifact;
                    try {
                        String directory = artifactToUnDeploy.getType();
                        Deployer deployer = getDeployer(directory,
                                                        artifactToUnDeploy.getFileExtension());
                        deployer.unDeploy(artifactToUnDeploy);
                        deployedArtifacts.remove(artifact);
                    } catch (CarbonDeploymentException e) {
                        log.error(e);
                    }
                }
            }
        } finally {
            artifactsToUnDeploy.clear();
        }
    }

    /**
     * Check and add artifacts which are removed form the repository to the artifacts to be
     * unDeployed list
     */
    public void checkUnDeployedArtifacts() {
        Iterator artifactFilePaths = deployedArtifacts.keySet().iterator();
        List toBeRemoved = new ArrayList();
        while (artifactFilePaths.hasNext()) {
            String filePath = (String) artifactFilePaths.next();
            Artifact artifact = (Artifact) deployedArtifacts.get(filePath);
            boolean found = false;
            for (int i = 0; i < artifactsList.size(); i++) {
                String artifactFilePath = (String) artifactsList.get(i);
                if (filePath.equals(artifactFilePath)) {
                    found = true;
                }
            }
            if (!found) {
                toBeRemoved.add(filePath);
                this.addArtifactToUnDeploy(artifact);
            }
        }

        for (int i = 0; i < toBeRemoved.size(); i++) {
            String fileName = (String) toBeRemoved.get(i);
            deployedArtifacts.remove(fileName);
        }
        toBeRemoved.clear();
        artifactsList.clear();
    }
}
