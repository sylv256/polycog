package gay.sylv.polycog.impl.bootstrap.sprocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public final class SprocketFileSystemProvider extends FileSystemProvider {
	private final SprocketFileSystem fs;

	public SprocketFileSystemProvider() {
		this.fs = new SprocketFileSystem(this);
	}

	@Override
	public String getScheme() {
		return "sprocket";
	}

	@Override
	public FileSystem newFileSystem(
			URI uri,
			Map<String, ?> env
	) {
		return this.fs;
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		return this.fs;
	}

	@Override
	public Path getPath(URI uri) {
		return fs.getPath(uri.getPath());
	}

	@Override
	public SeekableByteChannel newByteChannel(
			Path path,
			Set<? extends OpenOption> options,
			FileAttribute<?>... attrs
	) throws IOException {
		if (options.contains(StandardOpenOption.CREATE) || options.contains(StandardOpenOption.CREATE_NEW) || options.contains(StandardOpenOption.WRITE)) {
			throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
		}

		byte[] buf = SprocketClassLoader.INSTANCE.resources.get(path.toString());
		ReadableByteChannel rbc = Channels.newChannel(new ByteArrayInputStream(buf));
		return new SeekableByteChannel() {
			private long read = 0;

			@Override
			public int read(ByteBuffer dst) throws IOException {
				int n = rbc.read(dst);

				if (n > 0) {
					read += 1;
				}

				return n;
			}

			@Override
			public int write(ByteBuffer src) {
				throw new NonWritableChannelException();
			}

			@Override
			public long position() {
				return read;
			}

			@Override
			public SeekableByteChannel position(long newPosition) {
				read = newPosition;
				return this;
			}

			@Override
			public long size() {
				return buf.length;
			}

			@Override
			public SeekableByteChannel truncate(long size) {
				throw new NonWritableChannelException();
			}

			@Override
			public boolean isOpen() {
				return rbc.isOpen();
			}

			@Override
			public void close() throws IOException {
				rbc.close();
			}
		};
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(
			Path dir,
			DirectoryStream.Filter<? super Path> filter
	) throws IOException {
		return null;
	}

	@Override
	public void createDirectory(
			Path dir,
			FileAttribute<?>... attrs
	) throws IOException {
		throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
	}

	@Override
	public void delete(Path path) throws IOException {
		throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
	}

	@Override
	public void copy(
			Path source,
			Path target,
			CopyOption... options
	) throws IOException {
		throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
	}

	@Override
	public void move(
			Path source,
			Path target,
			CopyOption... options
	) throws IOException {
		throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
	}

	@Override
	public boolean isSameFile(
			Path path,
			Path path2
	) {
		return path.equals(path2);
	}

	@Override
	public boolean isHidden(Path path) {
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) {
		return this.fs.getFileStores().iterator().next();
	}

	@Override
	public void checkAccess(
			Path path,
			AccessMode... modes
	) {
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(
			Path path,
			Class<V> type,
			LinkOption... options
	) {
		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(
			Path path,
			Class<A> type,
			LinkOption... options
	) {
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(
			Path path,
			String attributes,
			LinkOption... options
	) {
		return Map.of();
	}

	@Override
	public void setAttribute(
			Path path,
			String attribute,
			Object value,
			LinkOption... options
	) throws IOException {
		throw new IOException("SprocketFileSystem is read-only (JARs are unmodifiable in memory)");
	}
}
