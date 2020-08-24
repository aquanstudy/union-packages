# -*- coding: utf-8 -*-
#Author:xiaohei
#CreateTime:2018-11-09
#
# manifest merger
#
# 2019.07.29: add file provider merge function. aar -> sdk folder -> decompile.   
# sdk folder -> decompile need cache file provider path. because of res are still not merged when manifest merged.
#

import file_utils
import os
import os.path
import config_utils
from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree
import os
import os.path
import zipfile
import re
import subprocess
import platform
from xml.dom import minidom
import codecs
import sys
import shutil
import time
from PIL import Image
import image_utils
import log_utils
import assets_merger
from res_merger import ResourceMerger2
import manifest_utils
import temp_utils


androidNS = 'http://schemas.android.com/apk/res/android'

file_provider_full_name = 'provider_android.support.v4.content.FileProvider'

global_non_strict_component_names = ['meta-data_android.max_aspect', 
                                     'meta-data_android.support.VERSION', 
                                     'uses-library_org.apache.http.legacy',
                                     'meta-data_notch.config',
                                     file_provider_full_name]


class ManifestComponent(object):

    def __init__(self, typeName, name, node, strictMode):
        super(ManifestComponent, self).__init__()
        self.typeName = typeName
        self.name = name
        self.node = node
        self.strictMode = strictMode            #if nodes in application are duplicated, the merger will fail.



    def get_name(self):

        return self.name


    def get_type(self):

        return self.typeName


    def get_node(self):

        return self.node

    def full_name(self):

        return self.typeName+"_"+self.name

    def is_strict_mode(self):

        fullName = self.full_name()

        if(fullName in global_non_strict_component_names):
            #特殊组件，不作检查限制
            return False

        return self.strictMode




class ManifestFile(object):
    """AndroidManifest.xml"""

    TAG_USE_LIBRARY = "uses-library"
    TAG_USE_FEATURE = "uses-feature"
    TAG_USE_PERMISSION = "uses-permission"
    TAG_PERMISSION = "permission"
    TAG_USE_PERMISSION_V23 = "uses-permission-sdk-23"
    TAG_ACTIVITY_ALIAS = "activity-alias"

    firstCacheFileProviderPath = None


    def __init__(self, manifestFile):
        super(ManifestFile, self).__init__()
        self.manifestFile = manifestFile
        
        self.components = dict()     #full_name is key
        self.parse()


    @classmethod
    def create(cls, manifestFile):

        if manifestFile.endswith('SDKManifest.xml') or manifestFile.endswith('SDKManifest_portrait.xml') or manifestFile.endswith('SDKManifest_landscape.xml'):

            return SDKManifestFile(manifestFile)

        else:

            return ManifestFile(manifestFile)


    def path(self):

        return self.manifestFile


    def cache_file_provider(self, component):

        if ManifestFile.firstCacheFileProviderPath != None and len(ManifestFile.firstCacheFileProviderPath) > 0:
            log_utils.debug("already cached first file provider:"+ManifestFile.firstCacheFileProviderPath)
            return False

        if self.manifestFile.endswith('SDKManifest.xml') or self.manifestFile.endswith('SDKManifest_portrait.xml') or self.manifestFile.endswith('SDKManifest_landscape.xml'):
            #sdk plugin need cache.
            resFileName = self.get_file_provider_res_file_name(component)

            ManifestFile.firstCacheFileProviderPath = os.path.join(os.path.dirname(self.manifestFile), "res/xml", resFileName)        

            log_utils.debug("cache a new file provider res path:"+ManifestFile.firstCacheFileProviderPath)

            return True

        return False


    def parse_leaf_nodes(self, nodes, strictMode):

        key = '{' + androidNS + '}name'

        if nodes != None and len(nodes) > 0:

            for node in nodes:

                name = node.get(key)

                if name == None or len(name) == 0:
                    log_utils.warning("manifest merge ignored " + str(node))
                    continue

                typeName = node.tag

                component = ManifestComponent(typeName, name, node, strictMode)
                fullName = component.full_name()

                if file_provider_full_name == fullName:
                    self.cache_file_provider(component)

                if fullName not in self.components:

                    self.components[fullName] = component

                else:
                    
                    log_utils.warning("node "+ name +" duplicated in " + self.manifestFile)        


    def parse(self):

        if not os.path.exists(self.manifestFile):
            log_utils.debug("the manifest file not exists:"+self.manifestFile)
            return


        ET.register_namespace('android', androidNS)
        key = '{' + androidNS + '}name'

        tree = ET.parse(self.manifestFile)
        root = tree.getroot()

        self.parse_leaf_nodes(root.findall(ManifestFile.TAG_USE_PERMISSION), False)
        self.parse_leaf_nodes(root.findall(ManifestFile.TAG_USE_PERMISSION_V23), False)
        self.parse_leaf_nodes(root.findall(ManifestFile.TAG_USE_FEATURE), False)
        self.parse_leaf_nodes(root.findall(ManifestFile.TAG_USE_LIBRARY), False)
        self.parse_leaf_nodes(root.findall(ManifestFile.TAG_PERMISSION), True)



        applicationNode = root.find('application')
        if applicationNode is None:
            return

        self.parse_leaf_nodes(list(applicationNode), False)         #采用非严格模式， 相同组件， 舍弃一个
        
        return 
 


    def get_all_permissions(self):

        result = list()

        for n in self.components:

            component = self.components[n]
            ctype = component.get_type()

            if ctype == ManifestFile.TAG_USE_PERMISSION or ctype == ManifestFile.TAG_PERMISSION or ctype == ManifestFile.TAG_USE_FEATURE or ctype == ManifestFile.TAG_USE_LIBRARY or ctype == ManifestFile.TAG_USE_PERMISSION_V23:
                result.append(component)

        return result


    def get_components_in_application(self):

        result = list()

        for n in self.components:

            component = self.components[n]
            ctype = component.get_type()

            if ctype == ManifestFile.TAG_USE_PERMISSION or ctype == ManifestFile.TAG_PERMISSION or ctype == ManifestFile.TAG_USE_FEATURE or ctype == ManifestFile.TAG_USE_LIBRARY or ctype == ManifestFile.TAG_USE_PERMISSION_V23:
                continue

            else:
                result.append(component)


        return result          


    def is_exists(self, fullName):

        if fullName in self.components:
            return True

        return False


    def get_component_by_name(self, fullName):

        if fullName in self.components:
            return self.components[fullName]

        return None        


    def can_merge_to(self, targetManifest):

        for c in self.components:

            component = self.components[c]

            if not component.is_strict_mode():

                continue

            if targetManifest.is_exists(component.full_name()):
                log_utils.debug(component.full_name())
                log_utils.error("the node " + component.get_name() + " duplicated in " + self.manifestFile + " and " + targetManifest.manifestFile)
                return False


        return True


    def get_file_provider_res_file_name(self, component):

        key = '{' + androidNS + '}name'
        resourceKey = '{' + androidNS + '}resource'

        metaNodes = component.node.findall('meta-data')

        if metaNodes == None or len(metaNodes) == 0:
            log_utils.error("merge android.support.v4.content.FileProvider failed. no meta-data info")
            return None

        for node in metaNodes:
            name = node.get(key)

            if name == 'android.support.FILE_PROVIDER_PATHS':
                resFile = node.get(resourceKey)
                if(not resFile.startswith('@xml/')):
                    log_utils.error("merge android.support.v4.content.FileProvider failed. android.support.FILE_PROVIDER_PATHS error:"+resFile)
                    return False

                resFile = resFile[5:] + ".xml"  #remove @xml/  and append .xml file ext
                return resFile

        return None      


    def can_merge_file_providers(self, component, targetComponent):

        authoritiesKey = '{' + androidNS + '}authorities'

        authorities1 = component.node.get(authoritiesKey)
        authorities2 = targetComponent.node.get(authoritiesKey)

        packageName = temp_utils.getCurrPackageName()

        if "$" in authorities1:
            authorities1 = re.sub(r'\${.*}', packageName, authorities1)

        if "$" in authorities2:
            authorities2 = re.sub(r'\${.*}', packageName, authorities2)

        # log_utils.debug("authorities of provider:"+authorities1)
        # log_utils.debug("authorities of provider:"+authorities2)

        if authorities1 != authorities2:
            return False

        return True


    def merge_file_providers(self, component, sourceManifest):

        targetProviderComponent = self.get_component_by_name(file_provider_full_name)

        if targetProviderComponent == None:

            return True

        canMerge = self.can_merge_file_providers(component, targetProviderComponent)
        if not canMerge:
            log_utils.error("merge file providers failed. different authorities")
            return False

        targetProviderFileName = self.get_file_provider_res_file_name(targetProviderComponent)

        if targetProviderFileName == None:
            #res xml not exists
            return True

        sourceProviderFileName = self.get_file_provider_res_file_name(component)

        if sourceProviderFileName == None:
            #handle failed
            return False

        targetBasePath = os.path.dirname(self.manifestFile)
        targetFilePath = os.path.join(targetBasePath, "res/xml", targetProviderFileName)
        sourceFilePath = os.path.join(os.path.dirname(sourceManifest.manifestFile), "res/xml", sourceProviderFileName)

        if not os.path.exists(targetFilePath):
            log_utils.error("merge FileProvider. target file not exists."+targetFilePath + " now check to use first cached file provider path")
            if targetBasePath.endswith('decompile') and ManifestFile.firstCacheFileProviderPath != None and len(ManifestFile.firstCacheFileProviderPath) > 0:
                targetFilePath = ManifestFile.firstCacheFileProviderPath
               

        if not os.path.exists(targetFilePath):
            log_utils.error("merge FileProvider failed. target file not exists."+targetFilePath)
            return False             

        if not os.path.exists(sourceFilePath):
            log_utils.error("merge FileProvider failed. source file not exists."+sourceFilePath)
            return False

        ret = manifest_utils.merge_paths_for_provider(sourceFilePath, targetFilePath)

        if not ret:
            log_utils.error("FileProvider merge failed. " + sourceFilePath + " and " + targetFilePath + " has same name with different path")
            return False

        return ret



    def merge_with(self, targetManifest):

        ET.register_namespace('android', androidNS)
        key = '{' + androidNS + '}name'

        tree = ET.parse(self.manifestFile)
        root = tree.getroot()        

        permissions = targetManifest.get_all_permissions()

        for p in permissions:

            if self.is_exists(p.full_name()):
                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            root.append(p.node)


        applicationNode = root.find('application')
        if applicationNode is None:
            applicationNode = SubElement(root, 'application')

        components = targetManifest.get_components_in_application()

        aliasComponents = list()

        for p in components:

            if p.get_type() == ManifestFile.TAG_ACTIVITY_ALIAS:
                aliasComponents.append(p)
                continue

            if self.is_exists(p.full_name()):

                if(file_provider_full_name == p.full_name()):

                    log_utils.warning("FileProvider duplicated. merge " + targetManifest.manifestFile + " with " + self.manifestFile)
                    ret = self.merge_file_providers(p, targetManifest)

                    if not ret:
                        log_utils.error("file provider merge failed. please check the file provider info")
                        #return False       #如果合并provider失败，需要打包失败。这里直接return False

                    continue

                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            applicationNode.append(p.node)


        for p in aliasComponents:

            if self.is_exists(p.full_name()):
                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            applicationNode.append(p.node)            


        tree.write(self.manifestFile, 'UTF-8')

        return True   



class SDKManifestFile(ManifestFile):

    def __init__(self, manifestFilePath):
        super(SDKManifestFile, self).__init__(manifestFilePath)


    def parse(self):

        if not os.path.exists(self.manifestFile):
            log_utils.debug("the manifest file not exists:"+self.manifestFile)
            return


        ET.register_namespace('android', androidNS)
        key = '{' + androidNS + '}name'

        tree = ET.parse(self.manifestFile)
        root = tree.getroot()

        permissionNode = root.find('permissionConfig')

        if permissionNode != None:

            self.parse_leaf_nodes(permissionNode.findall(ManifestFile.TAG_USE_PERMISSION), False)
            self.parse_leaf_nodes(permissionNode.findall(ManifestFile.TAG_USE_PERMISSION_V23), False)            
            self.parse_leaf_nodes(permissionNode.findall(ManifestFile.TAG_USE_FEATURE), False)
            self.parse_leaf_nodes(permissionNode.findall(ManifestFile.TAG_USE_LIBRARY), False)            
            self.parse_leaf_nodes(permissionNode.findall(ManifestFile.TAG_PERMISSION), True)



        applicationNode = root.find('applicationConfig')
        if applicationNode is None:
            return

        self.parse_leaf_nodes(list(applicationNode), False)     #采用非严格模式， 相同组件，舍弃一个
        
        return 


    def merge_with(self, targetManifest):

        ET.register_namespace('android', androidNS)
        key = '{' + androidNS + '}name'

        tree = ET.parse(self.manifestFile)
        root = tree.getroot()  

        permissionNode = root.find('permissionConfig') 

        if permissionNode == None:

            permissionNode = SubElement(root, 'permissionConfig')   



        permissions = targetManifest.get_all_permissions()

        for p in permissions:

            if self.is_exists(p.full_name()):
                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            permissionNode.append(p.node)


        applicationNode = root.find('applicationConfig')
        if applicationNode is None:
            applicationNode = SubElement(root, 'applicationConfig')

        components = targetManifest.get_components_in_application()
        aliasComponents = list()

        for p in components:

            if p.get_type() == ManifestFile.TAG_ACTIVITY_ALIAS:
                aliasComponents.append(p)
                continue

            if self.is_exists(p.full_name()):

                if(file_provider_full_name == p.full_name()):

                    log_utils.warning("FileProvider duplicated. merge " + targetManifest.manifestFile + " with " + self.manifestFile)
                    ret = self.merge_file_providers(p, targetManifest)

                    if not ret:
                        log_utils.error("file provider merge failed. please check the file provider info")
                        return False

                    continue                

                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            applicationNode.append(p.node)


        for p in aliasComponents:

            if self.is_exists(p.full_name()):
                log_utils.warning("manifest merger ignore :"+p.get_name()+" in "+targetManifest.manifestFile)
                continue

            applicationNode.append(p.node)          


        tree.write(self.manifestFile, 'UTF-8')   

        return True




def merge(manifestFiles):

    if manifestFiles == None or len(manifestFiles) == 0:
        return True

    manifests = list()
    for manifest in manifestFiles:
        if os.path.exists(manifest):
            m = ManifestFile.create(manifest)
            manifests.append(m)
        else:
            log_utils.warning("manifest file not exists. just igore. "+manifest)


    if len(manifests) <= 1:
        log_utils.debug("manifest file no need to merge")
        return True

    baseManifest = manifests.pop(len(manifests) - 1)

    for manifest in manifests:

        if not manifest.can_merge_to(baseManifest):
            log_utils.error("manifest merge failed. %s and %s  has same node.", manifest.path(), baseManifest.path())
            return False

        ret = baseManifest.merge_with(manifest)

        if not ret:

            return False


    return True


def merge2(baseManifestFile, targetManifestFile):

    files = [baseManifestFile, targetManifestFile]

    return merge(files)



if __name__ == "__main__":

    res1 = file_utils.getFullPath("Base_AndroidManifest.xml")
    res2 = file_utils.getFullPath("SDKManifest.xml")

    resPaths = [res2, res1]


    merge2(res2, res1)




