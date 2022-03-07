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
      supportedSystems = [ "aarch64-darwin" "aarch64-linux" "x86_64-linux" "x86_64-darwin" ];

      forSystem = system:
        let
          pkgs = import nixpkgs {
            inherit system;
            overlays = [ (f: p: { mill = p.mill.override { jre = p.jdk17_headless; }; }) ];
          };
          jdk = pkgs.jdk17_headless;

          jvmInputs = with pkgs; [ jdk coursier mill sbt ];
          jvmHook = ''
            JAVA_HOME="${jdk}"
          '';
          jsInputs = with pkgs; [ nodejs yarn ];
          jsHook = ''
            yarn install
          '';
        in
        {
          devShells = {
            main = pkgs.mkShell {
              name = "tyrian-dev-shell";
              buildInputs = jvmInputs ++ jsInputs;
              shellHook = jvmHook + jsHook;
            };

            jvm = pkgs.mkShell {
              name = "tyrian-scala-dev-shell";
              buildInputs = jvmInputs;
              shellHook = jvmHook;
            };

            js = pkgs.mkShell {
              name = "tyrian-js-dev-shell";
              buildInputs = jsInputs;
              shellHook = jsHook;
            };
          };
        };
    in
    flake-utils.lib.eachSystem supportedSystems forSystem;

}
