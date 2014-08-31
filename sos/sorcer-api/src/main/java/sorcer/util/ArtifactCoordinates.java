/**
 * Copyright 2013, 2014 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Rafał Krupiński
 */
public class ArtifactCoordinates implements Comparable<ArtifactCoordinates>{
	final public static String PCKG_POM = "pom";
	final public static String PCKG_JAR = "jar";
    final public static String DEFAULT_PACKAGING = PCKG_JAR;
    
    public static final Map<String,String> PACKAGINGS = new HashMap<String, String>();
	private static final String MVN_SEP = ":";
    private static Pattern artifactPattern = Pattern.compile("([^: /\\\\]+):([^: /\\\\]+)((:([^: /\\\\]+)?(:([^: /\\\\]+))?)?:([^: /\\\\]+))?");

	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String packaging;

	/**
     * Type is usually derived from the packaging, this field allows to override it.
     */
    private String type;

    static {
        PACKAGINGS.put(PCKG_POM, PCKG_POM);
        PACKAGINGS.put(PCKG_JAR, PCKG_JAR);
        PACKAGINGS.put("maven-plugin", PCKG_JAR);
        PACKAGINGS.put("ejb", PCKG_JAR);
        PACKAGINGS.put("war", "war");
        PACKAGINGS.put("ear", "ear");
        PACKAGINGS.put("rar", "rar");
        PACKAGINGS.put("par", "par");
        PACKAGINGS.put("zip", "zip");

        PACKAGINGS.put("maven-archetype", PCKG_JAR);
        PACKAGINGS.put("eclipse-plugin", PCKG_JAR);
        PACKAGINGS.put("bundle", PCKG_JAR);
    }

    /**
	 * @param coords artifact coordinates in the form of
	 *               groupId:artifactId[[:packaging[:classifier]]:version]
	 * @throws IllegalArgumentException
	 */
	public static ArtifactCoordinates coords(String coords) {
        if(!isArtifact(coords))
            throw new IllegalArgumentException(
                    "Artifact coordinates must be in a form of groupId:artifactId[[:packaging[:classifier]]:version] " + coords);

		String[] coordSplit = coords.split(MVN_SEP);
		int length = coordSplit.length;
		String groupId = coordSplit[0];
		String artifactId = coordSplit[1];
		String packaging = DEFAULT_PACKAGING;
		String classifier = null;
		//if version is not specified it will be resolved by the Resolver#resolveVersion
		String version = null;

		if (length == 3) {
            if (PACKAGINGS.values().contains(coordSplit[2]))
                packaging = coordSplit[2];
            else
			    version = coordSplit[2];
		} else if (length == 4) {
			packaging = coordSplit[2];
			version = coordSplit[3];
		} else if (length == 5) {
			packaging = coordSplit[2];
			classifier = coordSplit[3];
			version = coordSplit[4];
		}

		return new ArtifactCoordinates(groupId, artifactId, packaging, version, classifier);
	}

    public static boolean isArtifact(String coords){
        return artifactPattern.matcher(coords).find();
	}

	public ArtifactCoordinates(String groupId, String artifactId, String packaging, String version, String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.packaging = packaging;
		this.version = version;
		this.classifier = classifier;
	}

	public static ArtifactCoordinates coords(String groupId, String artifactId, String version) {
		return new ArtifactCoordinates(groupId, artifactId, DEFAULT_PACKAGING, version, null);
	}

    public static ArtifactCoordinates coords(String groupId, String artifactId, String version, String packaging) {
        return new ArtifactCoordinates(groupId, artifactId, packaging, version, null);
    }

	public ArtifactCoordinates(String groupId, String artifactId, String version) {
		this(groupId, artifactId, DEFAULT_PACKAGING, version, null);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(groupId).append(MVN_SEP).append(artifactId);
        // don't add packaging if not necessary
        if (classifier != null || !DEFAULT_PACKAGING.equals(packaging))
            result.append(MVN_SEP).append(getType());
		if (classifier != null) {
			result.append(MVN_SEP).append(classifier);
		}
		if (version != null) {
			result.append(MVN_SEP).append(version);
		}
		return result.toString();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

    public void setVersion(String version) {
        this.version = version;
    }

	@Override
	public boolean equals(Object obj) {
		return obj != null && (obj == this || obj instanceof ArtifactCoordinates && equals((ArtifactCoordinates) obj));
	}

	public boolean equals(ArtifactCoordinates coords) {
		return groupId.equals(coords.groupId)
				&& artifactId.equals(artifactId)
				&& version.equals(coords.version)
				&& packaging.equals(coords.packaging)
				&& ((classifier == null && coords.classifier == null)
				|| (classifier != null && classifier.equals(coords.classifier)));
	}

	@Override
	public int hashCode() {
		return groupId.hashCode() * 31
				+ artifactId.hashCode() * 37
				+ version.hashCode() * 41
				+ packaging.hashCode() * 43
				+ (classifier == null ? 0 : classifier.hashCode() * 47);
	}

	/**
	 * Retrieve artifact coordinates from relative artifact path
	 *
	 * @param relativePath path to file in repository
	 * @return ArtifactCoordinates of file, or null if it's not possible to unresolve it
	 */
	public static ArtifactCoordinates unresolve(String relativePath) {
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}
		String[] path = relativePath.split("/");
		//${groupId=~s|:|/|}/${artifactId}/${version}/${artifact}-${version}[-${classifier}].${packaging}

		//so path must contain at least 4 elements
		if (path.length < 4) {
			return null;
		}

		int fileNameIdx = path.length - 1;

		String fileName = path[fileNameIdx];
		String version = path[fileNameIdx - 1];
		String artifactId = path[fileNameIdx - 2];

		int groupPathLen = path.length - 3;
		String groupId = StringUtils.join(path, ".", 0, groupPathLen);

		int lastDotIdx = fileName.lastIndexOf('.');
		if (lastDotIdx < 0 || lastDotIdx + 1 == fileName.length()) {
			//ups - no file extension?
			return null;
		}
		String packaging = fileName.substring(lastDotIdx + 1);

		String classifier = null;

		//length of name excluding potential classifier
		int idAndVersionLen = artifactId.length() + version.length() + 2;
		if (idAndVersionLen < lastDotIdx) {
			classifier = fileName.substring(idAndVersionLen, lastDotIdx);
		}
		return new ArtifactCoordinates(groupId, artifactId, packaging, version, classifier);
	}

	/**
	 * Compare by toString() value
	 */
	@Override
	public int compareTo(ArtifactCoordinates o) {
		return toString().compareTo(o.toString());
	}

    public boolean isVersionSnapshot(){
        return version.endsWith("-SNAPSHOT");
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        if (type != null)
            return type;
        if (PACKAGINGS.containsKey(packaging))
            return PACKAGINGS.get(packaging);
        //the default
        return DEFAULT_PACKAGING;
    }
}
