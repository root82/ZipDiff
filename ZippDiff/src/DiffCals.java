import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.util.StringUtils;


public class DiffCals {

    private ZipFile compareFile;
    private ZipFile withFile;
    
	/**
	 * Constructor taking 2 filenames to compare
	 * @throws java.io.IOException
	 */
    public DiffCals(String filename1, String filename2) throws java.io.IOException{
    	this(new File(filename1), new File(filename2));
    }

	/**
	 * Constructor taking 2 Files to compare
	 * @throws java.io.IOException
	 */
	public DiffCals(File f1, File f2) throws java.io.IOException {
		this(new ZipFile(f1),new ZipFile(f2));
	}
	
	/**
	 * Constructor taking 2 ZipFiles to compare
	 */
	public DiffCals(ZipFile zf1, ZipFile zf2) {
		compareFile = zf1;
		withFile = zf2;
	}
	protected boolean ignoreThisFile(String filepath, String filename){
	
		//todo, for checking TIBCO.xml, par, sar files
		boolean result;

		if (filename == null){
			result = false;
		} else {
			String uppercaseName = filename.toUpperCase();
			if (uppercaseName.endsWith("TIBCO.XML") 
					|| uppercaseName.endsWith(".SAR")
					|| uppercaseName.endsWith(".PAR")){
				result = true;
			} else {
				result = false;
			}

		}
		return result;
	}
	
	public static boolean isZipFile(String filename){
		boolean result;
		
		if (filename == null){
			result = false;
		} else {
			String uppercaseName = filename.toUpperCase();
			if (uppercaseName.endsWith(".SAR")){
				result = true;
			} else if (uppercaseName.endsWith(".PAR")){
				result = true;
//			} else if (uppercaseName.endsWith(".WAR")){
//				result = true;
//			} else if (uppercaseName.endsWith(".RAR")){
//				result = true;
//			} else if (uppercaseName.endsWith(".JAR")){
//				result = true;
//			} else if (uppercaseName.endsWith(".EAR")){
//				result = true;
//			} else if (uppercaseName.endsWith(".ZIP")){
//				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	protected void processEmbeddedZipFile(String prefix, InputStream is, Map m) throws IOException{
		ZipInputStream zis = new ZipInputStream(is);
		
		ZipEntry entry = zis.getNextEntry();
		
		while (entry != null){
			processZipEntry(prefix, entry, zis, m);
			zis.closeEntry();
			entry = zis.getNextEntry();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processZipEntry(String prefix, ZipEntry zipEntry, InputStream is, Map zipEntryMap) throws java.io.IOException{

		String name = prefix + zipEntry.getName();
		
		if(zipEntry.isDirectory()){
			zipEntryMap.put(name, zipEntry);
		} else if(isZipFile(name)){
			processEmbeddedZipFile(zipEntry.getName() + "/", is, zipEntryMap);
			zipEntryMap.put(name, zipEntry);
		} else {
			zipEntryMap.put(name, zipEntry);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected Map buildZipEntryMap(ZipFile zf) throws java.io.IOException{
	
		Map zipEntryMap = new HashMap();
		
		try{
			Enumeration entries = zf.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = (ZipEntry) entries.nextElement();
				InputStream is = null;
				try{
					is = zf.getInputStream(entry);
					processZipEntry("", entry, is, zipEntryMap);
				}finally{
					if (is != null){
						is.close();
					}
				}
			}
			
		}finally{
			zf.close();
		}
		
		return zipEntryMap;
	}
	
	
	protected boolean entriesMatch(ZipEntry compareZipEntry, ZipEntry withZipEntry){
		boolean result;
		
		result = (compareZipEntry.isDirectory() == withZipEntry.isDirectory())
				&& (compareZipEntry.getSize() == withZipEntry.getSize())
				&& (compareZipEntry.getCompressedSize() == withZipEntry.getCompressedSize())
				&& (compareZipEntry.getCrc() == withZipEntry.getCrc())
				&& (compareZipEntry.getName().equals(withZipEntry.getName()));
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Diffs findDifferences(Map compareMap, Map withMap){
		Diffs d = new Diffs();
		
		Set compareNamesSet = compareMap.keySet();
		Set withNamesSet = withMap.keySet();
		
		Set allNames = new HashSet();
		allNames.addAll(compareNamesSet);
		allNames.addAll(withNamesSet);
		
		Iterator iterAllNames = allNames.iterator();
		
		while (iterAllNames.hasNext()){
			String name = (String) iterAllNames.next();
			
			if (ignoreThisFile("",name)){
				// todo
			} else {
				if (compareNamesSet.contains(name) && (!withNamesSet.contains(name))){
					d.fileAdded(name, (ZipEntry) compareMap.get(name)); 
				} else if (withNamesSet.contains(name) && (!compareNamesSet.contains(name))){
					d.fileRemoved(name, (ZipEntry) withMap.get(name));
				} else if (compareNamesSet.contains(name) && withNamesSet.contains(name)){
					ZipEntry compareZipEntry = (ZipEntry) compareMap.get(name);
					ZipEntry withZipEntry = (ZipEntry) withMap.get(name);
					if (!entriesMatch(compareZipEntry, withZipEntry)){
						d.fileChanged(name, compareZipEntry, withZipEntry);
					}
				}
			}
		}
		
		return d;
	}
	
	@SuppressWarnings("rawtypes")
	protected Diffs calculateDifferences(ZipFile compareZipFile, ZipFile withZipFile) throws java.io.IOException{

		Map compareMap = buildZipEntryMap(compareZipFile),
				withMap = buildZipEntryMap(withZipFile);
		
		return findDifferences(compareMap, withMap);
	}
	
	public Diffs getDifferences() throws java.io.IOException{
		Diffs d = calculateDifferences(compareFile, withFile);
		d.setCompareFileName(compareFile.getName());
		d.setWithFileName(withFile.getName());
		
		return d;
	}
}
