import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


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

		return false;
	}
	
	@SuppressWarnings("rawtypes")
	protected void processZipEntry(String prefix, ZipEntry zipEntry, InputStream is, Map zipEntryMap){
		if (ignoreThisFile(prefix,zipEntry.getName())){
			
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
		
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	protected Diffs findDifferences(Map m1, Map m2){
		
		
		return null;
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
