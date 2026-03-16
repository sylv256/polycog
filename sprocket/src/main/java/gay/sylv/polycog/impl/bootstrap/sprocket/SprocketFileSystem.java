package gay.sylv.polycog.impl.bootstrap.sprocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;

final class SprocketFileSystem extends FileSystem {
	private final SprocketFileSystemProvider provider;

	final class SprocketStore extends FileStore {
		@Override
		public String name() {
			return SprocketFileSystem.this + "/";
		}

		@Override
		public String type() {
			return "sprocketfs";
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public long getTotalSpace() {
			return Long.MAX_VALUE;
		}

		@Override
		public long getUsableSpace() {
			return 0;
		}

		@Override
		public long getUnallocatedSpace() {
			return 0;
		}

		@Override
		public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
			return false;
		}

		@Override
		public boolean supportsFileAttributeView(String name) {
			return false;
		}

		@Override
		public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
			return null;
		}

		@Override
		public Object getAttribute(String attribute) {
			return null;
		}
	}

	final class PathDelegate implements Path {
		private final Path path;

		PathDelegate(Path path) {
			this.path = path;
		}

		@Override
		public FileSystem getFileSystem() {
			return SprocketFileSystem.this;
		}

		@Override
		public boolean isAbsolute() {
			return path.isAbsolute();
		}

		@Override
		public Path getRoot() {
			return path.getRoot();
		}

		@Override
		public Path getFileName() {
			return path.getFileName();
		}

		@Override
		public Path getParent() {
			return new PathDelegate(path.getParent());
		}

		@Override
		public int getNameCount() {
			return path.getNameCount();
		}

		@Override
		public Path getName(int index) {
			return new PathDelegate(path.getName(index));
		}

		@Override
		public Path subpath(
				int beginIndex,
				int endIndex
		) {
			return new PathDelegate(path.subpath(beginIndex, endIndex));
		}

		@Override
		public boolean startsWith(Path other) {
			return path.startsWith(other);
		}

		@Override
		public boolean endsWith(Path other) {
			return path.endsWith(other);
		}

		@Override
		public Path normalize() {
			return new PathDelegate(path.normalize());
		}

		@Override
		public Path resolve(Path other) {
			return new PathDelegate(path.resolve(other));
		}

		@Override
		public Path relativize(Path other) {
			return new PathDelegate(path.relativize(other));
		}

		@Override
		public URI toUri() {
			URI uri = path.toUri();

			try {
				return new URI("sprocket", uri.getHost(), uri.getRawPath(), uri.getRawFragment());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Path toAbsolutePath() {
			return this;
		}

		@Override
		public Path toRealPath(LinkOption... options) {
			return this;
		}

		@Override
		public WatchKey register(
				WatchService watcher,
				WatchEvent.Kind<?>[] events,
				WatchEvent.Modifier... modifiers
		) throws IOException {
			throw new IOException("WatchService unsupported by SprocketFileSystem");
		}

		@Override
		public int compareTo(Path other) {
			return path.compareTo(other);
		}
	}

	SprocketFileSystem(SprocketFileSystemProvider provider) {
		this.provider = provider;
	}

	@Override
	public FileSystemProvider provider() {
		return this.provider;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return List.of();
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		return List.of(new SprocketStore());
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return Set.of();
	}

	@Override
	public Path getPath(
			String first,
			String... more
	) {
		return new PathDelegate(Path.of(first + String.join("", more)));
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		return null;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new IOException("WatchService unsupported by SprocketFileSystem");
	}
}
