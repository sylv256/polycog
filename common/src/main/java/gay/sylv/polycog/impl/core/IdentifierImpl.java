/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.core;

import gay.sylv.polycog.api.core.Identifier;

public record IdentifierImpl(String namespace, String path) implements Identifier {
}
