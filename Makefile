#  BouMaton, a program to compute transformations on pictures.
#  Copyright (C) 2019  Frédéric Boulanger (frederic.boulanger@centralesupelec.fr)
# 
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
# 
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
# 
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.
APPNAME = BouMaton2
VERSION=2.0
REZDIR = $(APPNAME).app/Contents/Resources
JAVASRC = ImageUtils.java \
		  BouMatonStrings.java \
		  BouMatonStrings_fr.java \
		  BouMaton.java \
		  DesktopManager.java \
		  DesktopManagerApple.java
SOURCES = $(JAVASRC:%.java=src/%.java)
CLASSES = $(SOURCES:src/%.java=bin/%.class)
JARREZ = icone.gif buildtime
REZFILES = $(JARREZ:%=resources/%)
SRCDISTDIR = BouMaton-$(VERSION)-src
DISTDIR = BouMaton-$(VERSION)
JAVAHOME=/Library/Java/JavaVirtualMachines/jdk-11.0.1.jdk/Contents/Home
#JAVAHOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home
APPLEMRJ=/System/Library/Java/Extensions/MRJToolkit.jar
APPLEEAW=$(JAVAHOME)/jre/lib/rt.jar
JAVAC = $(JAVAHOME)/bin/javac
JAVA = $(JAVAHOME)/bin/java
JAR = $(JAVAHOME)/bin/jar
CP = ditto --norsrc
CC = gcc

app : $(APPNAME).jar JavaLauncher.c
	rm -rf $(APPNAME).app
	mkdir -p $(APPNAME).app/Contents/MacOS
	echo "APPLBMAT" > $(APPNAME).app/Contents/PkgInfo
	$(CP) MacOS/Info.plist $(APPNAME).app/Contents/
	$(CC) -o $(APPNAME).app/Contents/MacOS/JavaLauncher JavaLauncher.c
	mkdir -p $(REZDIR)/Java
	$(CP) icone/icone.icns $(REZDIR)/
	$(CP) $(APPNAME).jar $(REZDIR)/Java/
	setfile -a B $(APPNAME).app

jar : $(APPNAME).jar

$(APPNAME).jar : $(CLASSES) $(REZFILES) manifest-info
	rm -f $(APPNAME).jar
	cp $(REZFILES) bin/
	$(JAR) cmf manifest-info $(APPNAME).jar -C bin/ .

resources/buildtime:$(CLASSES)

# Special case: if java.awt.Desktop is not supported do not break the build
# that will be handled at runtime.
bin/DesktopManager.class : src/DesktopManager.java
	date "+%Y-%m-%d %H:%M:%S" > resources/buildtime
	$(JAVAC) -classpath bin -d bin $< || touch $@

# Same issue with com.apple.eawt.Application
bin/DesktopManagerApple.class : src/DesktopManagerApple.java
	date "+%Y-%m-%d %H:%M:%S" > resources/buildtime
	$(JAVAC) -classpath bin -d bin $< || touch $@

bin/%.class : src/%.java
	date "+%Y-%m-%d %H:%M:%S" > resources/buildtime
	$(JAVAC) -classpath bin -d bin $<

JavaLauncher.c : Makefile JavaLauncher.c.proto
	sed -e 's/$$(APPNAME)/$(APPNAME)/g' JavaLauncher.c.proto > JavaLauncher.c


doc :
	(cd doc; make doc)

srcdist :
	rm -rf $(SRCDISTDIR)
	mkdir $(SRCDISTDIR)
	mkdir $(SRCDISTDIR)/src
	$(CP) $(SOURCES) $(SRCDISTDIR)/src
	$(CP) manifest-info Makefile JavaLauncher.c.proto $(SRCDISTDIR)
	mkdir $(SRCDISTDIR)/resources
	$(CP) resources/* $(SRCDISTDIR)/resources
	mkdir $(SRCDISTDIR)/MacOS
	$(CP) MacOS/* $(SRCDISTDIR)/MacOS
	mkdir $(SRCDISTDIR)/icone
	$(CP) icone/* $(SRCDISTDIR)/icone
	mkdir $(SRCDISTDIR)/doc
	$(CP) doc/*.jpg $(SRCDISTDIR)/doc/
	$(CP) doc/*.tex $(SRCDISTDIR)/doc/
	$(CP) doc/*.txt $(SRCDISTDIR)/doc/
	$(CP) doc/Makefile $(SRCDISTDIR)/doc/
	tar zcf $(SRCDISTDIR).tgz $(SRCDISTDIR)
	rm -r $(SRCDISTDIR)

dist :
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	rm -f $(DISTDIR).dmg
	tar cf - $(APPNAME).app | tar xCf $(DISTDIR) -
	$(CP) LICENSE doc/BouMaton.pdf doc/BouMaton_fr.pdf $(DISTDIR)
	ln -s /Applications $(DISTDIR)/Applications
	hdiutil create -srcfolder $(DISTDIR) -format UDZO $(DISTDIR).dmg
	rm -r $(DISTDIR)

clean :
	rm -f *.class
	rm -f bin/*.class
	rm -f bin/buildtime resources/buildtime
	rm -f JavaLauncher.c
	(cd doc; make clean)

clobber : clean
	rm -rf $(APPNAME).jar $(APPNAME).app
	(cd doc; make clobber)
