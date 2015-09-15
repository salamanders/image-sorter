# image-sorter
Sorts your messy folder of image by date (file name, folder name, and exif info)

# TODO
Keeping/using interesting parent path names
Move to trash when exact duplicate
Error if reached 10_000 offset names


  private static final Pattern ONLY_DIGEST_PARENT = Pattern.compile("^\\d+$");
  private static final Pattern POINTLESS_PARENT = Pattern.compile("^\\d{1,4}[-\\._]\\d{1,2}[-\\._]\\d{1,4}$");
  private static final Pattern REMOVABLE_PARENT = Pattern.compile("^\\d{4}[-\\._]{0,1}\\d{0,2}[-\\._]{0,1}(.+)$");
  private static Path getTargetFolder(final Path filePath, final BasicFileAttributes attrs) throws ImageProcessingException, IOException {
    final Calendar bestDate = ImageDateExtractor.getBestDate(filePath, attrs);
    if (bestDate == null) {
      return null;
    }
    final String parentName = filePath.getParent().getFileName().toString();

    // Try to salvage some of the parent folder name
    if (!POINTLESS_PARENT.matcher(parentName).matches() && !ImageDateExtractor.YEAR_MONTH_MATCHER.matcher(parentName).matches()) {
      final Matcher m = REMOVABLE_PARENT.matcher(parentName);
      if (m.matches()) {
        newFolder += m.group(1) + File.separator;
      } else {
        if (ONLY_DIGEST_PARENT.matcher(parentName).matches()) {
          // ignore
        } else {
          newFolder += filePath.getParent().getFileName() + File.separator;
        }
      }
    }

  public static final Pattern YEAR_MONTH_MATCHER = Pattern.compile("^(\\d{4})\\D(\\d{2}).*$");
  private static final Logger LOG = Logger.getLogger(ImageDateExtractor.class.getName());
  private static Calendar getPathDate(final Path filePath) {
    for (Path segment = filePath; segment != null && segment.getFileName() != null; segment = segment.getParent()) {
      final Matcher m = YEAR_MONTH_MATCHER.matcher(segment.getFileName().toString());
      if (m.matches()) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
        cal.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
        if (isReasonableDate(cal)) {
          return cal;
        }
      }
    }
    return null;
  }
    newFolder = newFolder.replaceAll(" ", "");

    return Paths.get(newFolder);