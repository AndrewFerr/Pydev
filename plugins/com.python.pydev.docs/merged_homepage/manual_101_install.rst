.. contents::

.. _`Aptana Studio 3`: http://aptana.com/products/studio3


Note for users with Aptana Studio 3 (Beta)
==========================================

Pydev already comes preinstalled in `Aptana Studio 3`_, so, this step can be skipped (note that if `Aptana Studio 3`_ is used,
Pydev cannot be installed or update separately, as it must always be updated as a whole). 


Before starting the install
============================

The first thing to choose before the install is a folder where you have permissions to write (otherwise, 
remember to install Eclipse running as an Administrator and remember to run as an Administrator whenever 
any plugin needs to be updated).

Also, it seems Eclipse 3.6.0 has some issues installing in shared locations (in any OS). An example of such a location would 
be the Program Files directory on Windows (see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=322929), so, 
please use at least 3.6.1 if planning to install Eclipse/Pydev on a shared location.


Installing with the update site 
================================

**Note: Instructions are targeted at Eclipse 3.5 and 3.6**

To install Pydev and Pydev Extensions using the Eclipse Update Manager, you need to use the **Help > Install New Software...**
menu (note that in older versions, this would be the 'Find and Install' menu).

.. image:: images/install_menu.png
   :class: snap
   :align: center   

   
In the next screen, add the update site(s) you want to work with (**See below for a list with the** `available update sites`_).

.. image:: images/update_sites.png
   :class: snap
   :align: center   
   
   
.. _http://pydev.org/updates: http://pydev.org/updates
.. _http://pydev.org/nightly: http://pydev.org/nightly
.. _SourceForge download: http://sourceforge.net/projects/pydev/files/


_`Available update sites`
-------------------------------

    

:Main:

    * `http://pydev.org/updates`_
    
:Nightly builds: 
    
    * `http://pydev.org/nightly`_
      
      

After entering the update sites, select the update site you entered or 
select "All available sites" and add a filter for Pydev, so that it 
shows the contents of all the update sites that have Pydev, then select what you want to install and click 'Next'.


.. image:: images/update_sites2.png
   :class: snap
   :align: center   


Then 'Next' again to confirm your selection

.. image:: images/update_sites3.png
   :class: snap
   :align: center   

And finally, read the license agreement and if you accept, select the accept radio button and click 'Finish'. 


.. image:: images/update_sites4.png
   :class: snap
   :align: center   
   
At that point, Eclipse should automatically download the plugin contents and present you to a dialog asking 
if you want to restart (to which you should say **yes**).

Commom install problems
------------------------
   
If you have any problem at this point with a message such as:

    ::
    
        An error occurred while collecting items to be installed
         No repository found containing:
        org.python.pydev/osgi.bundle/1.4.7.2843
         No repository found containing:
        org.python.pydev.ast/osgi.bundle/1.4.7.2843

that might indicate that the mirror you selected is having some network problem at that time, 
so, please follow the same steps with another mirror.


Installing with the zip file
==============================

The available locations for the zip files are:

* `SourceForge download`_
    

After downloading the zip file:

**Eclipse 3.4 and 3.5**

Extract the contents of the zip file in the **eclipse/dropins** folder and restart Eclipse.

**Before Eclipse 3.4**

Extract the contents of the zip file on top of Eclipse, making sure the plugins folder is extracted on top of the 
**eclipse/plugins** folder and the features is on top of the **eclipse/features** folder.
After that, restart Eclipse with the '-clean' flag, so that Eclipse finds out about it.



Checking the installation
===========================

You can verify if it is correctly installed going to the menu **'window > preferences'** and 
checking if there is a **Pydev** item under that.


Uninstalling
==============

Follow the instructons below if at any time you wish to stop using the Pydev plugin 
(or any other Eclipse plugin):

**Eclipse 3.5**

If you installed with the update site, go to the menu **help > about > installation details** then on the 
**Installed Software** tab, select the plugins you want to uninstall and click **Uninstall**.

If you installed with the zip file, just remove the com.python.pydev and org.python.pydev features and plugins from
the dropins folder.  

**Before Eclipse 3.4**

Go to the menu **help > software updates > manage configuration**, select the plugin and click 'disable', then, you have to restart Eclipse,
go to the same place again and then click on 'remove' (note that you have a button in the menu that enables you to see the 'disabled' features).


	
Corrupted install
======================


	Eclipse sometimes is not able to correctly get the plugin, from the update site but will do no checking
	on whether it is really correct (no md5 checking), and when this happens, you'll usually get a ClassNotFoundException
	(similar to the example below).

	When that happens, **you should uninstall it and reinstall again** with the update site... 
	if that still fails, you could try to get the zip files, as it will at least give you a warning when it is corrupt.
	
	Note that the chance of the files being corrupt in the server is pretty low, as that's something that's always checked 
	in a new release -- but if you're suspicious about it, please ask in the forum, so that it can be double-checked.

	Also, there have been reports with that error where the only solution that
	has been consistent has been **removing all** previous versions of pydev and then installing 
	the latest version.
	

**EXAMPLE**

	Unable to create this part due to an internal error. Reason for the failure:
	The editor class could not be instantiated. This usually indicates that the
	editor's class name was mistyped in plugin.xml.
	
	
	
	java.lang.ClassNotFoundException: org.python.pydev.editor.PyEdit 
	at org.eclipse.osgi.framework.internal.core.BundleLoader.findClass(BundleLoader.java:405)       
	at org.eclipse.osgi.framework.internal.core.BundleLoader.findClass(BundleLoader.java:350)
	at org.eclipse.osgi.framework.adaptor.core.AbstractClassLoader.loadClass(AbstractClassLoader.java:78)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:235)       
	at org.eclipse.osgi.framework.internal.core.BundleLoader.loadClass(BundleLoader.java:275)
	...
	
