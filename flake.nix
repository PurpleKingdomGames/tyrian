{
  description = "tyrian-dev";

  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixpkgs-unstable;
    flake-utils.url = github:numtide/flake-utils;
    flake-compat = {
      url = github:edolstra/flake-compat;
      flake = false;
    };
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    let
      supportedSystems = [ "aarch64-darwin" "aarch64-linux" "x86_64-linux" ];

      forSystem = system:
        let
          mill-overlay = f: p: {
            # top-level/all-packages.nix hardcodes `jre=jre8` when building mill so overriding jre alone does not work
            mill = p.mill.overrideAttrs (old: {
              installPhase = ''
                runHook preInstall
                install -Dm555 "$src" "$out/bin/.mill-wrapped"
                # can't use wrapProgram because it sets --argv0
                makeWrapper "$out/bin/.mill-wrapped" "$out/bin/mill" \
                  --prefix PATH : "${p.jdk17_headless}/bin" \
                  --set JAVA_HOME "${p.jdk17_headless}"
                runHook postInstall
              '';
            });
          };

          pkgs = import nixpkgs {
            inherit system;
            overlays = [ mill-overlay ];
          };
          jdk = pkgs.jdk17_headless;
        in
        {
          devShells = {
            jvm = pkgs.mkShell {
              name = "scala-dev-shell";
              buildInputs = [
                jdk
                pkgs.coursier
                pkgs.mill
                pkgs.sbt
              ];
              shellHook = ''
                JAVA_HOME="${jdk}"
              '';
            };

            js = pkgs.mkShell {
              name = "js-dev-shell";
              buildInputs = with pkgs; [
                nodejs
                yarn
              ];
            };
          };
        };
    in
    flake-utils.lib.eachSystem supportedSystems forSystem;

}
