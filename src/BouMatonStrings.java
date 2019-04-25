/*
 *  Use classes instead of xxx.properties files to avoid ISO-8859-1
 * 
 *   
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

public class BouMatonStrings extends ResourceBundle {
	private HashMap<String, String> strings = new HashMap<String, String>();
	
	public BouMatonStrings() {
		//// About item
		strings.put("AboutMenu", "About BouMaton...");
		//// Menus
		//  File menu
		strings.put("FileMenu", "File");
		strings.put("OpenItem", "Open...");
		strings.put("SaveItem", "Save...");
		strings.put("RevertItem", "Revert");
		strings.put("CloseItem", "Close");
		strings.put("QuitItem", "Quit");
		//  Transforms menu
		strings.put("TransformMenu", "Transforms");
		strings.put("BoulangerItem", "Boulanger");
		strings.put("PhotoMatonItem", "PhotoMaton");
		strings.put("InterruptItem", "Interrupt");
		strings.put("ShowInfoItem", "Show info");
		strings.put("HideInfoItem", "Hide info");
		strings.put("ShowHistoryItem", "Show history");
		strings.put("HideHistoryItem", "Hide history");
		//// Strings
		strings.put("NoPictureStr", "No picture");
		strings.put("BoulangerStr", "boulanger");
		strings.put("BoulangerCapStr", "Boulanger");
		strings.put("PhotoMatonStr", "photomaton");
		strings.put("PhotoMatonCapStr", "Photomaton");
		strings.put("SelectFileStr", "Select a file");
		strings.put("SaveAsStr", "Save as");
		strings.put("ErrorStr", "Error");
		strings.put("DismissStr", "Dismiss");
		strings.put("InTimeStr", "in");
		strings.put("PicturePeriodStr", "Picture period");
		strings.put("PixelPeriodStr", "Periods for individual pixels");
		strings.put("PixelsStr", "pixels");
		strings.put("PixelsPlaceStr", "pixels in place");
		strings.put("UnknownBuildStr", "<unknown date>");
		strings.put("BuiltOnStr", "built on");
		strings.put("OKStr", "OK");
		strings.put("CancelStr", "Cancel");
		strings.put("InformationStr", "Information");
		strings.put("PhotoTransInfoStr", "Photomaton transform info");
		strings.put("BoulangerTransInfoStr", "Boulanger transform info");
		strings.put("CloseStr", "Close");
		strings.put("HistoryStr", "History");
		strings.put("NoEnoughMemPeriodStr", "Not enough memory");
		strings.put("NoEnoughMemImageStr", "Not enough memory");
		strings.put("FreeMemStr", "Free memory:");
		strings.put("MaxMemStr", "Max memory:");
		strings.put("JPEGQualityChoiceStr", "Choose JPEG quality");
		strings.put("JPEGQualityStr", "JPEG quality:");
		strings.put("CalcJPEGSizeStr", "Compute file size");
		strings.put("ByteStr", "bytes");
		strings.put("KByteStr", "Kb");
		strings.put("MByteStr", "Mb");
		//// Help
		strings.put("JPEGQualityHelp", " JPEG format compresses pictures with quality loss\n  0-25: very low quality\n  25-50: low quality\n  50-75: average quality\n  75-90: good quality\n  90-100: best quality");
		strings.put("PNGHelp", " PNG format stores pictures without quality loss");
		//// Errors
		strings.put("MRJError", "Unexpected error while invoking MRJ method. Aborting.");
		strings.put("JPEGAvailError", "Could not find the JPEG encoder. Saving pictures is disabled.");
		strings.put("CouldNotReadStr", "Could not read");
		strings.put("CouldNotCreateStr", "Could not create");
		strings.put("CouldNotWriteStr", "Could not write to");
		strings.put("CannotSaveAsJPEGError", "Cannot save this kind of image as JPEG");
		strings.put("JPEGCodecError", "Problem while invoking JPEG codec methods");
		strings.put("NoMemPeriodsError", "Not enough memory to compute the period of the transforms");
	}

	@Override
	protected Object handleGetObject(String key) {
		return strings.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(strings.keySet());
	}

}
