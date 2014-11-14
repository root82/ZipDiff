import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;

@SuppressWarnings("rawtypes")
public class Diffs {
    private String compareFileName;
    private String withFileName;
    
    private final Map removed = new HashMap();
    private final Map added = new HashMap();
    private final Map changed = new HashMap();
    private final Map ignored = new HashMap();
    
	public String getCompareFileName() {
		return compareFileName;
	}
	public String getWithFileName() {
		return withFileName;
	}

	public void setCompareFileName(String compareFileName) {
		this.compareFileName = compareFileName;
	}
	public void setWithFileName(String withFileName) {
		this.withFileName = withFileName;
	}

	@SuppressWarnings("unchecked")
	public void fileRemoved(String fqn, ZipEntry ze){
		removed.put(fqn, ze);
	}

	@SuppressWarnings("unchecked")
	public void fileAdded(String fqn, ZipEntry ze){
		added.put(fqn, ze);
	}
	
	@SuppressWarnings("unchecked")
	public void fileIgnored(String fqn, ZipEntry ze){
		ignored.put(fqn, ze);
	}
	
	@SuppressWarnings("unchecked")
	public void fileChanged(String fqn, ZipEntry z1, ZipEntry z2){
		ZipEntry[] entries = new ZipEntry[2];
		entries[0] = z1;
		entries[1] = z2;
		changed.put(fqn, entries);
	}
	
	public Map getRemoved() {
		return removed;
	}
	
	public Map getAdded() {
		return added;
	}
	
	public Map getChanged() {
		return changed;
	}
	
	public Map getIgnored() {
		return ignored;
	}
	
	public boolean hasDifferences(){
		return ((getRemoved().size() > 0) || (getAdded().size() > 0) || (getChanged().size() > 0));
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		
		if (added.size() == 1){
			sb.append("1 file is");
		} else {
			sb.append(added.size() + " files are");
		}
		sb.append(" added to " + this.getCompareFileName() + "\n");
		
		Iterator iter = added.keySet().iterator();
		while(iter.hasNext()){
			String name = (String) iter.next();
			sb.append("\t[added] " + name + "\n");
		}
		
		if (removed.size() == 1){
			sb.append("1 file is");
		} else {
			sb.append(removed.size() + " files are");
		}
		sb.append(" removed from " + this.getCompareFileName() + "\n");
		
		iter = removed.keySet().iterator();
		while(iter.hasNext()){
			String name = (String) iter.next();
			sb.append("\t[removed] " + name + "\n");
		}
		
		if (changed.size() ==1){
			sb.append("1 file is");
		} else {
			sb.append(changed.size() + " files are");
		}
		sb.append(" changed in " + this.getCompareFileName() + "\n");

		iter = getChanged().keySet().iterator();
		while(iter.hasNext()){
			String name = (String) iter.next();
			ZipEntry[] entries = (ZipEntry[]) getChanged().get(name);
			sb.append("\t[changed] " + name + " ");
			sb.append(" (size " + entries[0].getSize());
			sb.append(" : " + entries[1].getSize());
			sb.append(" )\n");
		}				
		
		int diffCount = added.size() + removed.size() + changed.size();
		
		sb.append("Total differences: " + diffCount);
		
		return sb.toString();
	}
}
