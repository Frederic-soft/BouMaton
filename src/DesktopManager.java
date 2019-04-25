/*
 *  BouMaton, a program to compute transformations on pictures.
 *  Copyright (C) 2019  Frédéric Boulanger (frederic.boulanger@centralesupelec.fr)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.util.List;

public class DesktopManager implements AboutHandler, QuitHandler, OpenFilesHandler {
	private BouMaton myApp;
	private boolean handleAbout = false;
	
	public DesktopManager(BouMaton b) {
		Desktop desk = Desktop.getDesktop();
		myApp = b;
		if (desk.isSupported(Desktop.Action.APP_ABOUT)) {
			desk.setAboutHandler(this);
			handleAbout = true;
		}
		if (desk.isSupported(Desktop.Action.APP_OPEN_FILE)) {
			desk.setOpenFileHandler(this);
		}
		if (desk.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
			desk.setQuitHandler(this);
		}
	}
	
	public boolean getHandleAbout() {
		return handleAbout;
	}
	
	@Override
	public void openFiles(OpenFilesEvent e) {
		List<File> f = e.getFiles();
		if (f.size() > 0) {
			if (f.get(0).canRead()) {
				myApp.readFile(f.get(0));
			}
		}
	}


	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		if (response != null) {
			response.performQuit();
		}
		myApp.doQuit();
	}

	@Override
	public void handleAbout(AboutEvent e) {
		myApp.handleAbout();
	}

}
