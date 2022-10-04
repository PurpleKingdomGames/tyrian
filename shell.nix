{ shell ? "main", system ? builtins.currentSystem or "unknown-system" }:

(builtins.getFlake ("git+file://" + toString ./.)).devShells.${system}.${shell}
