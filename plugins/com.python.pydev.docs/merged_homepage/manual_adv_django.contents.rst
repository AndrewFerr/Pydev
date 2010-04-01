Django
---------

.. contents::



Pre-requisites
=======================

To get started with Django in Pydev, the pre-requisite is that Django is installed in the 
Python/Jython/IronPython interpreter you want to use (so, "import django" must properly work).

If you don't have Django installed, follow the steps from http://www.djangoproject.com/. 

Note that this tutorial won't teach you Django, only how the Django integration is available in Pydev, 
so, if you're not familiar with Django, it's useful to learn a bit about 
how it works and then use this help to know how the Pydev Django integration can help you. 



A bit on internals
~~~~~~~~~~~~~~~~~~~~

The Django integration in Pydev works through 3 main configurations:

1. The project must be marked as a Django project inside of Pydev.

2. A 'DJANGO_MANAGE_LOCATION' string substitution variable must point to the project-relative location of manage.py.

3. A 'DJANGO_SETTINGS_MODULE' string substitution variable must contain the name of the settings module in that project.

And that's it, with those properly configured, all the Django-related actions should work (and the steps below
will explain how those can be configured).



Creating a new project
=======================

Use the new project wizard through **Ctrl+N > Pydev Django Project** to create a new Django based project.

.. image:: images/django/pydev_django_project.png
   :class: snap   
   
That wizard will use **django-admin.py startproject** to create the project 
and will properly set the string substitution variables used inside of Pydev so that **DJANGO_MANAGE_LOCATION** points 
to the created manage.py location and the **DJANGO_SETTINGS_MODULE** points to the settings module.



Using the Django integration in an existing project
====================================================

For an existing project, just right click it and choose 'Pydev > Set as Django project'. 

.. image:: images/django/set_as_pydev_django_project.png
   :class: snap   
   
Note that the **DJANGO_MANAGE_LOCATION** and **DJANGO_SETTINGS_MODULE** will not be set at
this time, but when executing an action that needs it, Pydev will ask about it.




Django actions
================

Django actions can be accessed right clicking a project with the Django configuration and choosing Django > action.

.. image:: images/django/django_actions.png
   :class: snap   

Another option is using (with focus on a Pydev editor):

* **ctrl+2+dj <enter>** to open a dialog with the history of entered actions 
* **ctrl+2+dj action_to_execute <enter>** to already pass the action to be executed (e.g.: ctrl+2+dj help <enter>)

.. image:: images/django/django_ctrl_2.png
   :class: snap   



Interactive shell with Django
==============================

.. _`Interactive Console`: manual_adv_interactive_console.html
.. _Launching: manual_adv_launch.html


Note that the **Shell with django environment** action will create a shell properly configured with your settings, 
providing the default features from the pydev shell, such as code completion, history, context assistants 
(ctrl+1), making an execfile of the editor, etc (see: `Interactive Console`_ for more details).

It can be activated through **ctrl+2+dj shell** (if on a Pydev Editor) or right-clicking the project > **Django > Shell with django environment**.

.. image:: images/django/django_shell.png
   :class: snap   



Run/Debug as Django
====================


Run as Django/Debug as Django are available (note that they set the --no-reload by default, otherwise 
the debugger may have some issues and other processes may be kept alive when closing the process spawned by Pydev).

.. image:: images/django/run_as_django.png
   :class: snap   
   
This will create a default Run configuration, so, you may edit it later through run > run configurations (or debug > debug configurations)
if you want to change a parameter.

Note: to know how to rerun the last launch see: Rerun Last Launch on Launching_
