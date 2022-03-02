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
          pkgs = import nixpkgs {
            inherit system;
            overlays = [ (f: p: { jre8 = p.jdk17_headless; }) ];
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
