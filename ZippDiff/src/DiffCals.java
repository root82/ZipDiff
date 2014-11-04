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

    private ZipFile file1;
    private ZipFile file2;
    
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
		file1 = zf1;
		file2 = zf2;
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
					|| uppercaseName.endsWith(".PAR")
					|| uppercaseName.endsWith(".PAR")
					|| uppercaseName.endsWith(".XYZ")){
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
		if (ignoreThisFile(prefix,zipEntry.getName())){
			//todo
		} else{
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
	
	
	protected boolean entriesMatch(ZipEntry entry1, ZipEntry entry2){
		boolean result;
		
		result = 
				(entry1.isDirectory() == entry2.isDirectory())
				&& (entry1.getSize() == entry2.getSize())
				&& (entry1.getCompressedSize() == entry2.getCompressedSize())
//				&& (entry1.getCrc() == entry2.getCrc())
				&& (entry1.getName().equals(entry2.getName()));
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Diffs findDifferences(Map m1, Map m2){
		Diffs d = new Diffs();
		
		Set names1 = m1.keySet();
		Set names2 = m2.keySet();
		
		Set allNames = new HashSet();
		allNames.addAll(names1);
		allNames.addAll(names2);
		
		Iterator iterAllNames = allNames.iterator();
		
		while (iterAllNames.hasNext()){
			String name = (String) iterAllNames.next();
			
			if (names1.contains(name) && (!names2.contains(name))){
				d.fileRemoved(name, (ZipEntry) m1.get(name)); 
			} else if (names2.contains(name) && (!names1.contains(name))){
				d.fileAdded(name, (ZipEntry) m2.get(name));
			} else if (names1.contains(name) && names2.contains(name)){
				ZipEntry entry1 = (ZipEntry) m1.get(name);
				ZipEntry entry2 = (ZipEntry) m2.get(name);
				if (!entriesMatch(entry1, entry2)){
					d.fileChanged(name, entry1, entry2);
				}
			}
		}
		
		return d;
	}
	
	@SuppressWarnings("rawtypes")
	protected Diffs calculateDifferences(ZipFile zf1, ZipFile zf2) throws java.io.IOException{

		Map map1 = buildZipEntryMap(zf1),
				map2 = buildZipEntryMap(zf2);
		
		return findDifferences(map1, map2);
	}
	
	public Diffs getDifferences() throws java.io.IOException{
		Diffs d = calculateDifferences(file1, file2);
		d.setFilename1(file1.getName());
		d.setFilename2(file2.getName());
		
		return d;
	}
}
