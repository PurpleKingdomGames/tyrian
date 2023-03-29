{
  description = "tyrian-dev";

  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixpkgs-unstable;
    flake-utils.url = github:numtide/flake-utils;
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [
            (f: p: {
              mill = p.mill.override { jre = p.jdk17_headless; };
              sbt = p.sbt.override { jre = p.jdk17_headless; };
            })
          ];
        };
        jdk = pkgs.jdk17_headless;

        commonInputs = with pkgs; [
          chromedriver
          geckodriver
        ];

        jvmInputs = with pkgs; [
          jdk
          coursier
          mill
          sbt
        ];
        jvmHook = ''
          JAVA_HOME="${jdk}"
        '';

        jsInputs = with pkgs; [
          nodejs
          yarn
        ];
        jsHook = ''
          yarn install
        '';
      in
      {
        devShells.default = pkgs.mkShell {
          name = "tyrian-dev-shell";
          buildInputs = commonInputs ++ jvmInputs ++ jsInputs;
          shellHook = jvmHook + jsHook;
        };
      }
    );

}
